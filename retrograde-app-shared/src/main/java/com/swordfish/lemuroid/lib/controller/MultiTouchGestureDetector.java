package com.swordfish.lemuroid.lib.controller;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/* This source file has been included directly from here:
*  https://github.com/dinuscxj/MultiTouchGestureDetector/blob/master/multitouchgesturedetector/src/main/java/com/dinuscxj/gesture/MultiTouchGestureDetector.java
*  The library seems to be deprecated and removed from all the major repositories.
*  TODO... This should be removed when the touch controllers library will be migrated to compose.
* */
public class MultiTouchGestureDetector {
    public static final String TAG = "MultiTouchGestureDetector";

    public static final int MAX_ROTATION = 360;

    public static final float NO_SCALE = 1.0f;
    public static final float NO_ROTATE = 0.0f;
    public static final float NO_MOVE = 0.0f;

    private final Context mContext;
    private final OnMultiTouchGestureListener mListener;

    private float mCurrentFocusX;
    private float mCurrentFocusY;

    private float mPreviousFocusX;
    private float mPreviousFocusY;

    private float mCurrentSpan;
    private float mPreviousSpan;

    private float mCurrentRotation;
    private float mPreviousRotation;

    private long mCurrTime;
    private long mPrevTime;

    private boolean mInProgress;

    private float mInitialSpan;
    private int mSpanSlop;

    private float mInitialFocusX;
    private float mInitialFocusY;
    private int mTouchSlopSquare;

    /**
     * Creates a MultiTouchGestureDetector with the supplied listener.
     * You may only use this constructor from a {@link android.os.Looper Looper} thread.
     *
     * @param context the application's context
     * @param listener the listener invoked for all the callbacks, this must
     *          not be null.
     *
     * @throws NullPointerException if {@code listener} is null.
     */
    public MultiTouchGestureDetector(Context context, OnMultiTouchGestureListener listener) {
        mContext = context;
        mListener = listener;

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        int touchSlop = configuration.getScaledTouchSlop();
        mTouchSlopSquare = touchSlop * touchSlop;

        mSpanSlop = configuration.getScaledTouchSlop() * 2;
    }

    /**
     * Accepts MotionEvents and dispatches events to a {@link OnMultiTouchGestureListener}
     * when appropriate.
     *
     * <p>
     * Applications should pass a complete and consistent event stream to this method.
     * A complete and consistent event stream involves all MotionEvents from the initial
     * ACTION_DOWN to the final ACTION_UP or ACTION_CANCEL.
     * </p>
     *
     * @param event The event to process
     * @return true if the event was processed and the detector wants to receive the
     *         rest of the MotionEvents in this event stream.
     */
    public boolean onTouchEvent(MotionEvent event) {
        mCurrTime = event.getEventTime();

        final int action = event.getActionMasked();
        final int count = event.getPointerCount();

        final boolean touchComplete =
                action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL;
        final boolean touchStart = action == MotionEvent.ACTION_DOWN;

        if (touchStart || touchComplete) {
            if (mInProgress) {
                mListener.onEnd(this);
                mInProgress = false;
            }

            if (touchComplete) {
                return true;
            }
        }

        final boolean configChanged = action == MotionEvent.ACTION_DOWN
                || action == MotionEvent.ACTION_POINTER_UP
                || action == MotionEvent.ACTION_POINTER_DOWN;

        final boolean pointerUp = action == MotionEvent.ACTION_POINTER_UP;
        final int skipIndex = pointerUp ? event.getActionIndex() : -1;

        // Determine focal point
        float sumX = 0;
        float sumY = 0;

        final float focusX;
        final float focusY;

        final int div = pointerUp ? count - 1 : count;

        // compute focusX, focusY
        for (int i = 0; i < count; i++) {
            if (skipIndex == i) {
                continue;
            }

            sumX += event.getX(i);
            sumY += event.getY(i);
        }

        focusX = sumX / div;
        focusY = sumY / div;

        // Determine average deviation from focal point
        float devSumX = 0, devSumY = 0;
        for (int i = 0; i < count; i++) {
            if (skipIndex == i) {
                continue;
            }

            // Convert the resulting diameter into a radius.
            devSumX += Math.abs(event.getX(i) - focusX);
            devSumY += Math.abs(event.getY(i) - focusY);
        }
        final float devX = devSumX / div;
        final float devY = devSumY / div;

        // Span is the average distance between touch points through the focal point;
        // i.e. the diameter of the circle with a radius of the average deviation from
        // the focal point.
        final float spanX = devX * 2;
        final float spanY = devY * 2;

        final float span = (float) Math.hypot(spanX, spanY);

        // compute rotate
        float rotation = 0;
        outer: for (int i = 0; i < count; i++) {
            if (skipIndex == i) {
                continue;
            }

            inner: for (int j = i + 1; j < count; j++) {
                if (skipIndex == j) {
                    continue;
                }

                double deltaX = (event.getX(i) - event.getX(j));
                double deltaY = (event.getY(i) - event.getY(j));

                // Convert the resulting diameter into a radius.
                rotation +=
                        (Math.toDegrees(Math.atan2(deltaY, deltaX)) + MAX_ROTATION) % MAX_ROTATION;
                break outer;
            }
        }

        // Dispatch begin/end events as needed.
        // If the configuration changes, notify the app to reset its current state by beginning
        // a fresh scale event stream.
        final boolean wasInProgress = mInProgress;
        if (mInProgress && configChanged) {
            mListener.onEnd(this);
            mInProgress = false;
        }

        if (configChanged) {
            mInitialSpan = mPreviousSpan = mCurrentSpan = span;

            mInitialFocusX = mPreviousFocusX = mCurrentFocusX = focusX;
            mInitialFocusY = mPreviousFocusY = mCurrentFocusY = focusY;

            mPreviousRotation = mCurrentRotation = rotation;
        }

        if (!mInProgress && (wasInProgress
                || Math.abs(span - mInitialSpan) > mSpanSlop
                || Math.pow(mCurrentFocusX - mInitialFocusX, 2.0d) +
                Math.pow(mCurrentFocusY - mInitialFocusY, 2.0d) > mTouchSlopSquare)) {
            mPreviousSpan = mCurrentSpan = span;
            mPrevTime = mCurrTime;

            mPreviousFocusX = mCurrentFocusX = focusX;
            mPreviousFocusY = mCurrentFocusY = focusY;

            mPreviousRotation = mCurrentRotation = rotation;

            mInProgress = mListener.onBegin(this);
        }

        // Handle motion; focal point and span/scale factor are changing.
        if (action == MotionEvent.ACTION_MOVE) {
            mCurrentSpan = span;

            mCurrentFocusX = focusX;
            mCurrentFocusY = focusY;

            mCurrentRotation = rotation;

            if (mInProgress) {
                if (getScale() != NO_SCALE) {
                    mListener.onScale(this);
                }

                if (getRotation() != NO_ROTATE) {
                    mListener.onRotate(this);
                }

                if (getMoveX() != NO_MOVE || getMoveY() != NO_MOVE) {
                    mListener.onMove(this);
                }
            }

            mPreviousSpan = mCurrentSpan;

            mPreviousFocusX = mCurrentFocusX;
            mPreviousFocusY = mCurrentFocusY;

            mPreviousRotation = mCurrentRotation;

            mPrevTime = mCurrTime;
        }

        return true;
    }

    /**
     * Returns {@code true} if a scale gesture is in progress.
     */
    public boolean isInProgress() {
        return mInProgress;
    }

    /**
     * Get the X coordinate of the current gesture's focal point.
     * If a gesture is in progress, the focal point is between
     * each of the pointers forming the gesture.
     *
     * If {@link #isInProgress()} would return false, the result of this
     * function is undefined.
     *
     * @return X coordinate of the focal point in pixels.
     */
    public float getFocusX() {
        return mCurrentFocusX;
    }

    /**
     * Get the Y coordinate of the current gesture's focal point.
     * If a gesture is in progress, the focal point is between
     * each of the pointers forming the gesture.
     *
     * If {@link #isInProgress()} would return false, the result of this
     * function is undefined.
     *
     * @return Y coordinate of the focal point in pixels.
     */
    public float getFocusY() {
        return mCurrentFocusY;
    }

    /**
     * Return the X coordinate distance from the previous focus event to the current
     * event. This value is defined as
     * ({@link #mCurrentFocusX} - {@link #mPreviousFocusX}).
     *
     * @return X coordinate Distance between focal points in pixels.
     */
    public float getMoveX() {
        return mCurrentFocusX - mPreviousFocusX;
    }

    /**
     * Return the Y coordinate distance from the previous focus event to the current
     * event. This value is defined as
     * ({@link #mCurrentFocusY} - {@link #mPreviousFocusY}).
     *
     * @return Y coordinate Distance between focal points in pixels.
     */
    public float getMoveY() {
        return mCurrentFocusY - mPreviousFocusY;
    }

    /**
     * Return the average rotate between each of the pointers forming the
     * gesture in progress through the focal point.
     *
     * ({@link #mCurrentRotation} - {@link #mPreviousRotation}).
     *
     * @return rotate between pointers in degrees.
     */
    public float getRotation() {
        return mCurrentRotation - mPreviousRotation;
    }

    /**
     * Return the scaling factor from the previous scale event to the current
     * event. This value is defined as
     * ({@link #mCurrentSpan} / {@link #mPreviousSpan}).
     *
     * @return The current scaling factor.
     */
    public float getScale() {
        return mPreviousSpan > 0 ? mCurrentSpan / mPreviousSpan : 1;
    }

    /**
     * Return the time difference in milliseconds between the previous
     * accepted scaling event and the current scaling event.
     *
     * @return Time difference since the last scaling event in milliseconds.
     */
    public long getTimeDelta() {
        return mCurrTime - mPrevTime;
    }

    /**
     * Return the event time of the current event being processed.
     *
     * @return Current event time in milliseconds.
     */
    public long getEventTime() {
        return mCurrTime;
    }

    /**
     * The listener for receiving notifications when gestures occur.
     * If you want to listen for all the different gestures then implement
     * this interface. If you only want to listen for a subset it might
     * be easier to extend {@link SimpleOnMultiTouchGestureListener}.
     *
     * An application will receive events in the following order:
     * <ul>
     * <li>One {@link OnMultiTouchGestureListener#onBegin(MultiTouchGestureDetector)}
     * <li>Zero or more {@link OnMultiTouchGestureListener#onScale(MultiTouchGestureDetector)}
     * <li>One {@link OnMultiTouchGestureListener#onEnd(MultiTouchGestureDetector)}
     * </ul>
     */
    public interface OnMultiTouchGestureListener {
        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         *          retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         *         as handled. If an event was not handled, the detector
         *         will continue to accumulate movement until an event is
         *         handled. This can be useful if an application, for example,
         *         only wants to update scaling factors if the change is
         *         greater than 0.01.
         */
        void onScale(MultiTouchGestureDetector detector);


        /**
         * Responds to moving events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         *          retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         *         as handled. If an event was not handled, the detector
         *         will continue to accumulate movement until an event is
         *         handled. This can be useful if an application, for example,
         *         only wants to update scaling factors if the change is
         *         greater than 0.01.
         */
        void onMove(MultiTouchGestureDetector detector);

        /**
         * Responds to rotating events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         *          retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         *         as handled. If an event was not handled, the detector
         *         will continue to accumulate movement until an event is
         *         handled. This can be useful if an application, for example,
         *         only wants to update scaling factors if the change is
         *         greater than 0.01.
         */
        void onRotate(MultiTouchGestureDetector detector);

        /**
         * Responds to the beginning of a touch gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         *          retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         *         this gesture. For example, if a gesture is beginning
         *         with a focal point outside of a region where it makes
         *         sense, onBegin() may return false to ignore the
         *         rest of the gesture.
         */
        boolean onBegin(MultiTouchGestureDetector detector);

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         *
         * Once a touch has ended, {@link MultiTouchGestureDetector#getFocusX()}
         * and {@link MultiTouchGestureDetector#getFocusY()} will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         *          retrieve extended info about event state.
         */
        void onEnd(MultiTouchGestureDetector detector);
    }

    public static class SimpleOnMultiTouchGestureListener implements OnMultiTouchGestureListener {

        public void onScale(MultiTouchGestureDetector detector) {}

        @Override
        public void onMove(MultiTouchGestureDetector detector) {}

        @Override
        public void onRotate(MultiTouchGestureDetector detector) {}

        public boolean onBegin(MultiTouchGestureDetector detector) {
            return true;
        }

        public void onEnd(MultiTouchGestureDetector detector) {
            // Intentionally empty
        }
    }
}
