package phy.jsf

import phy.jsf.bt.BtService
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testData() {
        var service = BtService()
        var command = service.createCommand(POWER_NONE, WORK_MODE_DRY, WIND_SPEED_L2, 20, TIME_1)
        println(command.data.joinToString())
    }
}
