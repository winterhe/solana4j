package com.lmax.solana4j.programs;

import com.lmax.solana4j.Solana;
import com.lmax.solana4j.api.PublicKey;
import com.lmax.solana4j.api.TransactionBuilder;
import com.lmax.solana4j.api.TransactionInstruction;
import com.lmax.solana4j.encoding.SolanaEncoding;
import com.lmax.solana4j.encoding.SysVar;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Program for managing system-level operations on the blockchain.
 *
 * <p>
 * This class provides methods to create accounts, transfer funds, and manage nonce accounts on the solana blockchain.
 * </p>
 */
public final class SystemProgram
{
  private static final byte[] SYSTEM_PROGRAM_ID = SolanaEncoding.decodeBase58("11111111111111111111111111111111");

  /**
   * The public key for the system program account.
   */
  public static final PublicKey SYSTEM_PROGRAM_ACCOUNT = Solana.account(SYSTEM_PROGRAM_ID);

  /**
   * The length of a nonce account in bytes.
   */
  public static final int NONCE_ACCOUNT_LENGTH = 80;

  /**
   * The length of a mint account in bytes.
   */
  public static final int MINT_ACCOUNT_LENGTH = 82;

  /**
   * the instruction DISCRIMINATOR LENGTH;
   */
  public static final int NATIVE_DISCRIMINATOR_LENGTH = 4;

  /**
   * The instruction code for creating an account.
   */
  public static final int CREATE_ACCOUNT_INSTRUCTION = 0;

  /**
   * The instruction code for transferring funds.
   */
  public static final int TRANSFER_INSTRUCTION = 2;

  /**
   * https://github.com/solana-labs/solana/blob/master/sdk/program/src/system_instruction.rs
   * The instruction code for Create a new account at an address derived from a base pubkey and a seed
   */
  public static final int CREATE_ACCOUNT_WITH_SEED = 3;

  /**
   * The instruction code for advancing a nonce.
   */
  public static final int ADVANCE_NONCE_INSTRUCTION = 4;

  /**
   * The instruction code for initializing a nonce account.
   */
  public static final int NONCE_INIT_INSTRUCTION = 6;

  private SystemProgram()
  {
  }

  /**
   * Factory method for creating a new instance of {@code SystemProgramFactory}.
   *
   * @param tb the transaction builder
   * @return a new instance of {@code SystemProgramFactory}
   */
  public static SystemProgramFactory factory(final TransactionBuilder tb)
  {
    return new SystemProgramFactory(tb);
  }

  /**
   * Inner factory class for building system program instructions using a {@link TransactionBuilder}.
   */
  public static final class SystemProgramFactory
  {

    private final TransactionBuilder tb;

    private SystemProgramFactory(final TransactionBuilder tb)
    {
      this.tb = tb;
    }

    /**
     * Creates a new account.
     *
     * @param payer      the public key of the payer
     * @param newAccount the public key of the new account
     * @param lamports   the number of lamports to transfer to the new account
     * @param space      the amount of space to allocate for the new account
     * @param program    the public key of the program to associate with the new account
     * @return this {@code SystemProgramFactory} instance
     */
    public SystemProgramFactory createAccount(final PublicKey payer, final PublicKey newAccount, final long lamports, final long space, final PublicKey program)
    {
      tb.append(SystemProgram.createAccount(payer, newAccount, lamports, space, program));
      return this;
    }

    /**
     * Initializes a nonce account.
     *
     * @param nonce      the public key of the nonce account
     * @param authorized the public key of the authorized account
     * @return this {@code SystemProgramFactory} instance
     */
    public SystemProgramFactory nonceInitialize(final PublicKey nonce, final PublicKey authorized)
    {
      tb.append(SystemProgram.nonceInitialize(nonce, authorized));
      return this;
    }

    /**
     * Advances a nonce.
     *
     * @param nonce      the public key of the nonce account
     * @param authorized the public key of the authorized account
     * @return this {@code SystemProgramFactory} instance
     */
    public SystemProgramFactory nonceAdvance(final PublicKey nonce, final PublicKey authorized)
    {
      tb.append(SystemProgram.nonceAdvance(nonce, authorized));
      return this;
    }

    /**
     * Transfers funds between accounts.
     *
     * @param from     the public key of the account to transfer funds from
     * @param to       the public key of the account to transfer funds to
     * @param lamports the amount of lamports to transfer
     * @return this {@code SystemProgramFactory} instance
     */
    public SystemProgramFactory transfer(final PublicKey from, final PublicKey to, final long lamports)
    {
      tb.append(SystemProgram.transfer(from, to, lamports));
      return this;
    }

    /**
     //https://github.com/solana-labs/solana/blob/master/sdk/program/src/system_instruction.rs
     // Create a new account at an address derived from a base pubkey and a seed
     //
     // # Account references
     //   0. '[WRITE, SIGNER]' Funding account
     //   1. '[WRITE]' Created account
     //   2. '[SIGNER]' (optional) Base account; the account matching the base Pubkey below must be
     //                          provided as a signer, but may be the same as the funding account
     //                          and provided as account 0
     * @param fromPublicKey      PublicKey: fromSigner(Base) public key
     * @param accountWithSeed PublicKey: the PublicKey create with seed
     * @param base PublicKey: Base public key
     * @param seed String of ASCII chars, no longer than 'Pubkey::MAX_SEED_LEN'
     * @param lamports u64: Number of lamports to transfer to the new account
     * @param space u64: Number of bytes of memory to allocate
     * @param owner PublicKey: Owner program account address
     * @return A {@code TransactionInstruction} of the created instruction
     */
    public SystemProgramFactory createAccountWithSeed(
        final PublicKey fromPublicKey, final PublicKey accountWithSeed,
        final PublicKey base,
        final String seed, final long lamports, final long space,
        final PublicKey owner) {
      tb.append(SystemProgram.createAccountWithSeed(
          fromPublicKey, accountWithSeed,
          base,
          seed, lamports, space, owner));
      return this;
    }

  }


  /**
   * Creates a new account.
   *
   * @param payer      the public key of the payer
   * @param newAccount the public key of the new account
   * @param lamports   the number of lamports to transfer to the new account
   * @param space      the amount of space to allocate for the new account
   * @param program    the public key of the program to associate with the new account
   * @return A {@code TransactionInstruction} of the created instruction
   */
  public static TransactionInstruction createAccount(final PublicKey payer, final PublicKey newAccount, final long lamports, final long space, final PublicKey program)
  {
    return Solana.instruction(ib -> ib
        .program(SYSTEM_PROGRAM_ACCOUNT)
        .account(payer, true, true)
        .account(newAccount, true, true)
        .data(52, bb ->
        {
          bb.order(ByteOrder.LITTLE_ENDIAN)
              .putInt(CREATE_ACCOUNT_INSTRUCTION)
              .putLong(lamports)
              .putLong(space);
          program.write(bb);
        })
    );
  }

  /**
   * Initializes a nonce account.
   *
   * @param nonce      the public key of the nonce account
   * @param authorized the public key of the authorized account
   * @return A {@code TransactionInstruction} of the created instruction
   */
  public static TransactionInstruction nonceInitialize(final PublicKey nonce, final PublicKey authorized)
  {
    return Solana.instruction(ib -> ib
        .program(SYSTEM_PROGRAM_ACCOUNT)
        .account(nonce, false, true)
        .account(SysVar.RECENT_BLOCKHASHES, false, false)
        .account(SysVar.RENT, false, false)
        .data(36, bb ->
        {
          bb.order(ByteOrder.LITTLE_ENDIAN)
              .putInt(NONCE_INIT_INSTRUCTION);
          authorized.write(bb);
        })
    );
  }

  /**
   * Advances a nonce.
   *
   * @param nonce      the public key of the nonce account
   * @param authorized the public key of the authorized account
   * @return A {@code TransactionInstruction} of the created instruction
   */
  public static TransactionInstruction nonceAdvance(final PublicKey nonce, final PublicKey authorized)
  {
    return Solana.instruction(ib -> ib
        .program(SYSTEM_PROGRAM_ACCOUNT)
        .account(nonce, false, true)
        .account(SysVar.RECENT_BLOCKHASHES, false, false)
        .account(authorized, true, false)
        .data(4, bb -> bb.order(ByteOrder.LITTLE_ENDIAN)
            .putInt(ADVANCE_NONCE_INSTRUCTION))
    );
  }

  /**
   * Transfers funds between accounts.
   *
   * @param from     the public key of the account to transfer funds from
   * @param to       the public key of the account to transfer funds to
   * @param lamports the amount of lamports to transfer
   * @return A {@code TransactionInstruction} of the created instruction
   */
  public static TransactionInstruction transfer(final PublicKey from, final PublicKey to, final long lamports)
  {
    return Solana.instruction(ib -> ib
        .program(SYSTEM_PROGRAM_ACCOUNT)
        .account(from, true, true)
        .account(to, false, true)
        .data(12, bb -> bb.order(ByteOrder.LITTLE_ENDIAN)
            .putInt(TRANSFER_INSTRUCTION)
            .putLong(lamports))
    );
  }

  /**
   //https://github.com/solana-labs/solana/blob/master/sdk/program/src/system_instruction.rs
   // Create a new account at an address derived from a base pubkey and a seed
   //
   // # Account references
   //   0. '[WRITE, SIGNER]' Funding account
   //   1. '[WRITE]' Created account
   //   2. '[SIGNER]' (optional) Base account; the account matching the base Pubkey below must be
   //                          provided as a signer, but may be the same as the funding account
   //                          and provided as account 0
   * @param fromPublicKey      PublicKey: fromSigner(Base) public key
   * @param accountWithSeed PublicKey: the PublicKey create with seed
   * @param base PublicKey: Base public key
   * @param seed String of ASCII chars, no longer than 'Pubkey::MAX_SEED_LEN'
   * @param lamports u64: Number of lamports to transfer to the new account
   * @param space u64: Number of bytes of memory to allocate
   * @param owner PublicKey: Owner program account address
   * @return A {@code TransactionInstruction} of the created instruction
   */
  public static TransactionInstruction createAccountWithSeed(
      final PublicKey fromPublicKey, final PublicKey accountWithSeed,
      final PublicKey base,
      final String seed, final long lamports, final long space,
      final PublicKey owner) {
    final byte[] seedBytes = seed.getBytes(StandardCharsets.UTF_8);
    int dateLength = NATIVE_DISCRIMINATOR_LENGTH
        + PublicKey.PUBLIC_KEY_LENGTH
        + (Long.BYTES + seedBytes.length)
        + Long.BYTES
        + Long.BYTES
        + PublicKey.PUBLIC_KEY_LENGTH;

    boolean isSigner = !fromPublicKey.equals(base);

    return Solana.instruction(ib -> ib
        .program(SYSTEM_PROGRAM_ACCOUNT)
        .account(fromPublicKey, true, true)
        .account(accountWithSeed, false, true)
        // if base not same as payer(fromPublicKey),
        // then base must sign the transaction(ReadOnlySigner)
        .account(base, isSigner, false)
        .data(dateLength, bb ->
        {
          // 1. 指令类型 (3 = CreateAccountWithSeed) - 4字节小端
          bb.order(ByteOrder.LITTLE_ENDIAN).putInt(CREATE_ACCOUNT_WITH_SEED);
          // 2. base pubkey (32 bytes)
          bb.put(base.bytes());
          // 3. seed (u64 length + bytes)
          // 3.1 Seed 长度 - 8字节小端
          bb.order(ByteOrder.LITTLE_ENDIAN).putLong(seedBytes.length);
          // 3.2 Seed 内容
          bb.put(seedBytes);
          bb.order(ByteOrder.LITTLE_ENDIAN)
              // 4. Lamports - 8字节小端
              .putLong(lamports)
              // 5. space (u64) (Nonce Account 固定 80) - 8字节小端
              .putLong(space);
          bb.put(owner.bytes());
        })
    );
  }

  /**
     * Gets the nonce value from a nonce account's data.
     *
     * @param nonceAccountDataBytes the byte array containing the nonce account's data
     * @return the public key representing the nonce value
     */
  public static PublicKey getNonceAccountValue(final byte[] nonceAccountDataBytes)
  {
    assert nonceAccountDataBytes.length >= 72;

    final ByteBuffer nonceAccountData = ByteBuffer.allocate(nonceAccountDataBytes.length);
    nonceAccountData.put(nonceAccountDataBytes);

    final byte[] nonceValue = new byte[32];
    nonceAccountData.position(40);
    nonceAccountData.get(nonceValue, 0, 32);

    return Solana.account(nonceValue);
  }
}
