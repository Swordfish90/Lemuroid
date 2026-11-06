package com.swordfish.lemuroid.app.shared.game.viewmodel

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MenuPressTrackerTest {
    private val tracker = MenuPressTracker(300, 500)

    @Test
    fun singleTapDoesNotOpenMenu() {
        tracker.press(0)
        assertFalse(tracker.release(60))
    }

    @Test
    fun twoQuickTapsOpenMenuOnSecondRelease() {
        tracker.press(0)
        assertFalse(tracker.release(60))
        tracker.press(150)
        assertTrue(tracker.release(210))
    }

    @Test
    fun tapsOutsideTimeoutDoNotOpenMenu() {
        tracker.press(0)
        assertFalse(tracker.release(60))
        tracker.press(400)
        assertFalse(tracker.release(460))
    }

    @Test
    fun longPressOpensOnlyOnceAndReleaseDoesNotOpenAgain() {
        tracker.press(0)
        assertFalse(tracker.longPress(499))
        assertTrue(tracker.longPress(500))
        assertFalse(tracker.longPress(600))
        assertFalse(tracker.release(650))
    }

    @Test
    fun tapAfterLongPressDoesNotCountAsDoubleTap() {
        tracker.press(0)
        assertTrue(tracker.longPress(500))
        assertFalse(tracker.release(550))
        tracker.press(600)
        assertFalse(tracker.release(650))
    }

    @Test
    fun duplicatePressesAndReleasesCannotCreateDoubleTaps() {
        assertTrue(tracker.press(0))
        assertFalse(tracker.press(20))
        assertFalse(tracker.release(60))
        assertFalse(tracker.release(70))
    }

    @Test
    fun releaseWithoutPressDoesNotOpenMenu() {
        assertFalse(tracker.release(50))
        assertFalse(tracker.longPress(1000))
    }

    @Test
    fun lifecycleResetDiscardsPendingTapAndHold() {
        tracker.press(0)
        tracker.release(50)
        tracker.press(100)
        tracker.reset()
        assertFalse(tracker.longPress(700))
        assertFalse(tracker.release(710))
        tracker.press(720)
        assertFalse(tracker.release(750))
    }

    @Test
    fun tripleTapOpensOnlyOnce() {
        tracker.press(0)
        tracker.release(50)
        tracker.press(100)
        assertTrue(tracker.release(150))
        tracker.press(200)
        assertFalse(tracker.release(250))
    }

    @Test
    fun delayedLongPressCallbackStillAllowsOpeningOnRelease() {
        tracker.press(0)
        assertTrue(tracker.release(550))
        assertFalse(tracker.longPress(600))
    }
}
