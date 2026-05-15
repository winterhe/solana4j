package com.lmax.solana4j.encoding;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

final class Hash {
  static {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }

  public static MessageDigest sha256Digest() {
    try {
      return MessageDigest.getInstance("SHA-256", BouncyCastleProvider.PROVIDER_NAME);
    } catch (final NoSuchAlgorithmException | NoSuchProviderException e) {
      throw new RuntimeException("Unable to find SHA-256", e);
    }
  }

  public static MessageDigest sha512Digest() {
    try {
      return MessageDigest.getInstance("SHA-512", BouncyCastleProvider.PROVIDER_NAME);
    } catch (final NoSuchAlgorithmException | NoSuchProviderException e) {
      throw new RuntimeException("Unable to find SHA-512", e);
    }
  }

  public static byte[] sha256(final byte[] input) {
    return sha256Digest().digest(input);
  }

  public static byte[] sha256Twice(final byte[] bytes) {
    return sha256Twice(bytes, 0, bytes.length);
  }

  public static byte[] sha256Twice(final byte[] bytes, final int offset, final int length) {
    final var digest = sha256Digest();
    digest.update(bytes, offset, length);
    digest.update(digest.digest());
    return digest.digest();
  }

  public static byte[] h160(final byte[] input) {
    final byte[] sha256 = sha256(input);
    final var digest = new RIPEMD160Digest();
    digest.update(sha256, 0, sha256.length);
    final byte[] out = new byte[20];
    digest.doFinal(out, 0);
    return out;
  }

  private Hash() {
  }
}