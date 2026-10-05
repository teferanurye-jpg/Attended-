package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.location.GeofenceCalculator
import com.example.util.EthioDateTimeUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Ethio Attendance", appName)
  }

  @Test
  fun `geofence distance calculation within radius`() {
    // Addis HQ coordinates: 9.0227, 38.7468
    val distance = GeofenceCalculator.calculateDistanceMeters(
        9.0227, 38.7468,
        9.0227, 38.7468
    )
    assertEquals(0f, distance, 0.01f)
  }

  @Test
  fun `punctuality check identifies arrival time correctly`() {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 8)
    cal.set(Calendar.MINUTE, 25)
    assertTrue("Arrival at 8:25 AM should be punctual", EthioDateTimeUtil.isPunctual(cal.timeInMillis))
  }
}
