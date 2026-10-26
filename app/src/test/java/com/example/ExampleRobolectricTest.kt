package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("TaskFlow", appName)
    }

    @Test
    fun `task priority mapping works correctly`() {
        assertEquals(TaskPriority.HIGH, TaskPriority.fromString("high"))
        assertEquals(TaskPriority.MEDIUM, TaskPriority.fromString("medium"))
        assertEquals(TaskPriority.LOW, TaskPriority.fromString("low"))
        assertEquals(TaskPriority.MEDIUM, TaskPriority.fromString("unknown"))
    }

    @Test
    fun `task category icons and colors are non null`() {
        assertNotNull(TaskCategory.getIconForCategory("Work"))
        assertNotNull(TaskCategory.getColorForCategory("Work"))
    }
}
