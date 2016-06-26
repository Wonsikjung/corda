package com.r3corda.core.node

import com.r3corda.core.contracts.*
import com.r3corda.core.crypto.SecureHash
import com.r3corda.core.node.services.Wallet
import com.r3corda.core.testing.DUMMY_NOTARY
import org.junit.Test
import java.security.PublicKey
import kotlin.test.assertEquals


class WalletUpdateTests {

    object DummyContract : Contract {

        override fun verify(tx: TransactionForContract) {
        }

        override val legalContractReference: SecureHash = SecureHash.sha256("")
    }

    private class DummyState : ContractState {
        override val participants: List<PublicKey>
            get() = emptyList()
        override val contract = WalletUpdateTests.DummyContract
    }

    private val stateRef0 = StateRef(SecureHash.randomSHA256(), 0)
    private val stateRef1 = StateRef(SecureHash.randomSHA256(), 1)
    private val stateRef2 = StateRef(SecureHash.randomSHA256(), 2)
    private val stateRef3 = StateRef(SecureHash.randomSHA256(), 3)
    private val stateRef4 = StateRef(SecureHash.randomSHA256(), 4)

    private val stateAndRef0 = StateAndRef(TransactionState(DummyState(), DUMMY_NOTARY), stateRef0)
    private val stateAndRef1 = StateAndRef(TransactionState(DummyState(), DUMMY_NOTARY), stateRef1)
    private val stateAndRef2 = StateAndRef(TransactionState(DummyState(), DUMMY_NOTARY), stateRef2)
    private val stateAndRef3 = StateAndRef(TransactionState(DummyState(), DUMMY_NOTARY), stateRef3)
    private val stateAndRef4 = StateAndRef(TransactionState(DummyState(), DUMMY_NOTARY), stateRef4)

    @Test
    fun `nothing plus nothing is nothing`() {
        val before = Wallet.NoUpdate
        val after = before + Wallet.NoUpdate
        assertEquals(before, after)
    }

    @Test
    fun `something plus nothing is something`() {
        val before = Wallet.Update(setOf(stateRef0, stateRef1), setOf(stateAndRef2, stateAndRef3))
        val after = before + Wallet.NoUpdate
        assertEquals(before, after)
    }

    @Test
    fun `nothing plus something is something`() {
        val before = Wallet.NoUpdate
        val after = before + Wallet.Update(setOf(stateRef0, stateRef1), setOf(stateAndRef2, stateAndRef3))
        val expected = Wallet.Update(setOf(stateRef0, stateRef1), setOf(stateAndRef2, stateAndRef3))
        assertEquals(expected, after)
    }

    @Test
    fun `something plus consume state 0 is something without state 0 output`() {
        val before = Wallet.Update(setOf(stateRef2, stateRef3), setOf(stateAndRef0, stateAndRef1))
        val after = before + Wallet.Update(setOf(stateRef0), setOf())
        val expected = Wallet.Update(setOf(stateRef2, stateRef3), setOf(stateAndRef1))
        assertEquals(expected, after)
    }

    @Test
    fun `something plus produce state 4 is something with additional state 4 output`() {
        val before = Wallet.Update(setOf(stateRef2, stateRef3), setOf(stateAndRef0, stateAndRef1))
        val after = before + Wallet.Update(setOf(), setOf(stateAndRef4))
        val expected = Wallet.Update(setOf(stateRef2, stateRef3), setOf(stateAndRef0, stateAndRef1, stateAndRef4))
        assertEquals(expected, after)
    }

    @Test
    fun `something plus consume states 0 and 1, and produce state 4, is something without state 0 and 1 outputs and only state 4 output`() {
        val before = Wallet.Update(setOf(stateRef2, stateRef3), setOf(stateAndRef0, stateAndRef1))
        val after = before + Wallet.Update(setOf(stateRef0, stateRef1), setOf(stateAndRef4))
        val expected = Wallet.Update(setOf(stateRef2, stateRef3), setOf(stateAndRef4))
        assertEquals(expected, after)
    }
}