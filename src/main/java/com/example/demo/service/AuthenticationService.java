package com.example.demo.service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.request.AuthenticationRequest;
import com.example.demo.dto.request.IntrospectRequest;
import com.example.demo.dto.request.response.AuthenticationResponse;
import com.example.demo.dto.request.response.IntrospectResponse;
import com.example.demo.entity.InvalidatedToken;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.InvalidatedTokenRepository;
import com.example.demo.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
  UserRepository userRepository;
  InvalidatedTokenRepository invalidatedTokenRepository;

  @NonFinal
  @Value("${jwt.signerKey}")
  protected String SIGNER_KEY;

  public IntrospectResponse introspect(IntrospectRequest request)
      throws JOSEException, ParseException {
    var token = request.getToken();
    JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
    SignedJWT signedJWT = SignedJWT.parse(token);
    Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
    var verified = signedJWT.verify(verifier);

    boolean isInvalidated =
        invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID());

    return IntrospectResponse.builder()
        .valid(verified && expiryTime.after(new Date()) && !isInvalidated)
        .build();
  }

  public void logout(IntrospectRequest request) throws JOSEException, ParseException {
    var token = request.getToken();
    SignedJWT signedJWT = SignedJWT.parse(token);
    String jit = signedJWT.getJWTClaimsSet().getJWTID();
    Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

    InvalidatedToken invalidatedToken =
        InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();
    invalidatedTokenRepository.save(invalidatedToken);
  }

  public AuthenticationResponse refreshToken(IntrospectRequest request)
      throws JOSEException, ParseException {
    // Invalidate old token
    logout(request);

    // Parse old token to get username
    SignedJWT signedJWT = SignedJWT.parse(request.getToken());
    String username = signedJWT.getJWTClaimsSet().getSubject();

    // Verify user still exists
    userRepository
        .findByUsername(username)
        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

    // Generate new token
    var newToken = generateToken(username);
    return AuthenticationResponse.builder().token(newToken).authenticated(true).build();
  }

  public AuthenticationResponse authenticate(AuthenticationRequest request) {
    log.info("SignKey: {}", SIGNER_KEY);
    var user =
        userRepository
            .findByUsername(request.getUsername())
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
    boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
    if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);

    var token = generateToken(request.getUsername());
    return AuthenticationResponse.builder().token(token).authenticated(true).build();
  }

  private String generateToken(String username) {
    JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

    JWTClaimsSet jwtClaimsSet =
        new JWTClaimsSet.Builder()
            .subject(username)
            .issuer("devteria.com")
            .issueTime(new Date())
            .expirationTime(new Date(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()))
            .jwtID(UUID.randomUUID().toString())
            .claim("customClaim", "Custom")
            .build();
    Payload payload = new Payload(jwtClaimsSet.toJSONObject());
    JWSObject jwsObject = new JWSObject(header, payload);
    try {
      jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
      return jwsObject.serialize();
    } catch (JOSEException e) {
      log.error("Cannot create token", e);
      throw new RuntimeException(e);
    }
  }
}
