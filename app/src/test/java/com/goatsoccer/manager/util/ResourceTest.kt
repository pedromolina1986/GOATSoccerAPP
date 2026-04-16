package com.goatsoccer.manager.util

import org.junit.Assert.*
import org.junit.Test

class ResourceTest {

    @Test
    fun `Success wraps data and has null message`() {
        val resource = Resource.Success("token123")

        assertTrue(resource is Resource.Success)
        assertEquals("token123", resource.data)
        assertNull(resource.message)
    }

    @Test
    fun `Success works with different data types`() {
        val intResource = Resource.Success(42)
        val listResource = Resource.Success(listOf("a", "b"))

        assertEquals(42, intResource.data)
        assertEquals(listOf("a", "b"), listResource.data)
    }

    @Test
    fun `Error wraps message and has null data by default`() {
        val error = Resource.Error<String>("Something went wrong")

        assertTrue(error is Resource.Error)
        assertEquals("Something went wrong", error.message)
        assertNull(error.data)
    }

    @Test
    fun `Error can carry fallback data alongside message`() {
        val error = Resource.Error("Partial failure", data = "stale_data")

        assertEquals("Partial failure", error.message)
        assertEquals("stale_data", error.data)
    }

    @Test
    fun `Loading has no data and no message`() {
        val loading = Resource.Loading<String>()

        assertTrue(loading is Resource.Loading)
        assertNull(loading.data)
        assertNull(loading.message)
    }

    @Test
    fun `Resource types are distinguishable via is-check`() {
        val success: Resource<String> = Resource.Success("ok")
        val error: Resource<String> = Resource.Error("fail")
        val loading: Resource<String> = Resource.Loading()

        assertTrue(success is Resource.Success)
        assertFalse(success is Resource.Error)

        assertTrue(error is Resource.Error)
        assertFalse(error is Resource.Loading)

        assertTrue(loading is Resource.Loading)
        assertFalse(loading is Resource.Success)
    }
}
