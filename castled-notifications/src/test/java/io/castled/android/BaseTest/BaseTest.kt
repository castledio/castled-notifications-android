package io.castled.android.BaseTest

import org.junit.jupiter.api.BeforeAll


abstract class BaseTest {
    companion object {
        @BeforeAll
        @JvmStatic
        fun setUpBeforeAllTests() {
            // Initialization code here
            System.out.println("Initialization before all tests")
        }
    }
}