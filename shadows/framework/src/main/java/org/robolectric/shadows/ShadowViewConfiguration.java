/*
 * Portions of this code came from frameworks/base/core/java/android/view/ViewConfiguration.java,
 * which contains the following license text:
 *
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.ViewConfiguration;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ViewConfiguration.class)
public class ShadowViewConfiguration {
  @RealObject ViewConfiguration realViewConfiguration;

  private static final int PRESSED_STATE_DURATION = 125;
  private static final int LONG_PRESS_TIMEOUT = 500;
  private static final int TAP_TIMEOUT = 115;
  private static final int DOUBLE_TAP_TIMEOUT = 300;
  private static final int TOUCH_SLOP = 16;
  private static final int PAGING_TOUCH_SLOP = TOUCH_SLOP * 2;
  private static final int DOUBLE_TAP_SLOP = 100;
  private static final int WINDOW_TOUCH_SLOP = 16;
  private static final int MAXIMUM_FLING_VELOCITY = 4000;

  // The previous hardcoded value for draw cache size. Some screenshot tests depend on this value.
  private static final int MIN_MAXIMUM_DRAWING_CACHE_SIZE = 480 * 800 * 4;

  private int edgeSlop;
  private int fadingEdgeLength;
  private int minimumFlingVelocity;
  private int maximumFlingVelocity;
  private int touchSlop;
  private int pagingTouchSlop;
  private int doubleTapSlop;
  private int windowTouchSlop;
  private int maximumDrawingCacheSize;
  private static boolean hasPermanentMenuKey = true;

  private void setup(Context context) {
    final Resources resources = context.getResources();
    final DisplayMetrics metrics = resources.getDisplayMetrics();
    float density = metrics.density;
    final Configuration config = resources.getConfiguration();
    final float sizeAndDensity;
    if (config.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_XLARGE)) {
      sizeAndDensity = density * 1.5f;
    } else {
      sizeAndDensity = density;
    }
    edgeSlop = (int) (density * ViewConfiguration.getEdgeSlop() + 0.5f);
    fadingEdgeLength = (int) (density * ViewConfiguration.getFadingEdgeLength() + 0.5f);
    minimumFlingVelocity = (int) (density * ViewConfiguration.getMinimumFlingVelocity() + 0.5f);
    maximumFlingVelocity = (int) (density * ViewConfiguration.getMaximumFlingVelocity() + 0.5f);
    int scrollbarSize;
    if (RuntimeEnvironment.getApiLevel() >= P) {
      scrollbarSize =
          Resources.getSystem()
              .getDimensionPixelSize(
                  reflector(AndroidInternalDimenReflector.class).getConfigScrollbarSize());
    } else {
      scrollbarSize = (int) (density * ViewConfiguration.getScrollBarSize() + 0.5f);
    }
    reflector(ViewConfigurationReflector.class, realViewConfiguration)
        .setScrollbarSize(scrollbarSize);
    int baseOverflingDistance =
        reflector(ViewConfigurationReflector.class).getBaseOverflingDistance();
    reflector(ViewConfigurationReflector.class, realViewConfiguration)
        .setOverflingDistance((int) (sizeAndDensity * baseOverflingDistance));
    touchSlop = (int) (density * TOUCH_SLOP + 0.5f);
    pagingTouchSlop = (int) (density * PAGING_TOUCH_SLOP + 0.5f);
    doubleTapSlop = (int) (density * DOUBLE_TAP_SLOP + 0.5f);
    windowTouchSlop = (int) (density * WINDOW_TOUCH_SLOP + 0.5f);
    // Some screenshot tests were misconfigured and try to draw very large views onto small
    // screens using SW rendering. To avoid breaking these tests, we keep the drawing cache a bit
    // larger when screens are configured to be arbitrarily small.
    // TODO(hoisie): Investigate removing this Math.max logic.
    maximumDrawingCacheSize =
        Math.max(MIN_MAXIMUM_DRAWING_CACHE_SIZE, 4 * metrics.widthPixels * metrics.heightPixels);
    boolean enableFadingMarquee =
        resources.getBoolean(
            reflector(AndroidInternalBoolReflector.class).getEnableFadingMarquee());
    reflector(ViewConfigurationReflector.class, realViewConfiguration)
        .setFadingMarqueeEnabled(enableFadingMarquee);
    if (RuntimeEnvironment.getApiLevel() >= Q) {
      int minScalingSpan =
          useRealMinScalingSpan()
              ? resources.getDimensionPixelSize(
                  reflector(AndroidInternalDimenReflector.class).getConfigMinScalingSpan())
              : 0;
      reflector(ViewConfigurationReflector.class, realViewConfiguration)
          .setMinScalingSpan(minScalingSpan);
    }
  }

  @Implementation
  protected static ViewConfiguration get(Context context) {
    ViewConfiguration viewConfiguration = new ViewConfiguration();
    ShadowViewConfiguration shadowViewConfiguration = Shadow.extract(viewConfiguration);
    shadowViewConfiguration.setup(context);

    if (RuntimeEnvironment.getApiLevel() >= Q) {
      reflector(ViewConfigurationReflector.class, viewConfiguration)
          .setConstructedWithContext(true);
    }

    return viewConfiguration;
  }

  @Implementation
  protected int getScaledFadingEdgeLength() {
    return fadingEdgeLength;
  }

  @Implementation
  protected static int getPressedStateDuration() {
    return PRESSED_STATE_DURATION;
  }

  @Implementation
  protected static int getLongPressTimeout() {
    return LONG_PRESS_TIMEOUT;
  }

  @Implementation
  protected static int getTapTimeout() {
    return TAP_TIMEOUT;
  }

  @Implementation
  protected static int getDoubleTapTimeout() {
    return DOUBLE_TAP_TIMEOUT;
  }

  @Implementation
  protected int getScaledEdgeSlop() {
    return edgeSlop;
  }

  @Implementation
  protected static int getTouchSlop() {
    return TOUCH_SLOP;
  }

  @Implementation
  protected int getScaledTouchSlop() {
    return touchSlop;
  }

  @Implementation
  protected int getScaledPagingTouchSlop() {
    return pagingTouchSlop;
  }

  @Implementation
  protected int getScaledDoubleTapSlop() {
    return doubleTapSlop;
  }

  @Implementation
  protected static int getWindowTouchSlop() {
    return WINDOW_TOUCH_SLOP;
  }

  @Implementation
  protected int getScaledWindowTouchSlop() {
    return windowTouchSlop;
  }

  @Implementation
  protected int getScaledMinimumFlingVelocity() {
    return minimumFlingVelocity;
  }

  @Implementation
  protected static int getMaximumFlingVelocity() {
    return MAXIMUM_FLING_VELOCITY;
  }

  @Implementation
  protected int getScaledMaximumFlingVelocity() {
    return maximumFlingVelocity;
  }

  @Implementation
  protected int getScaledMaximumDrawingCacheSize() {
    return maximumDrawingCacheSize;
  }

  @Implementation
  protected boolean hasPermanentMenuKey() {
    return hasPermanentMenuKey;
  }

  public static void setHasPermanentMenuKey(boolean value) {
    hasPermanentMenuKey = value;
  }

  @ForType(ViewConfiguration.class)
  interface ViewConfigurationReflector {
    @Static
    @Accessor("OVERFLING_DISTANCE")
    int getBaseOverflingDistance();

    @Accessor("mConstructedWithContext")
    void setConstructedWithContext(boolean value);

    @Accessor("mScrollbarSize")
    void setScrollbarSize(int value);

    @Accessor("mFadingMarqueeEnabled")
    void setFadingMarqueeEnabled(boolean enableFadingMarquee);

    @Accessor("mOverflingDistance")
    void setOverflingDistance(int value);

    @Accessor("mMinScalingSpan")
    void setMinScalingSpan(int minScalingSpan);
  }

  /**
   * Reflection is needed to access internal Android dimen constants, which are static final ints,
   * so referencing them directly causes inlining. Note {@link AndroidInternalDimenReflector} is
   * designed to be temporary until the real {@link
   * android.view.ViewConfiguration#get(android.content.Context)} is called.
   */
  @ForType(com.android.internal.R.dimen.class)
  interface AndroidInternalDimenReflector {
    @Static
    @Accessor("config_scrollbarSize")
    int getConfigScrollbarSize();

    @Static
    @Accessor("config_minScalingSpan")
    int getConfigMinScalingSpan();
  }

  /**
   * Due to overzealous shadowing, the return value of {@link
   * ViewConfiguration#getScaledMinimumScalingSpan} was previously 0, when it should be around 170
   * for an mdpi display with density factor 1. This issue has been fixed, but there may be tests
   * that emit touch events to trigger pinching/spreading that rely on the previous incorrect
   * behavior. These tests have an option to use a system property to enable this previous bug.
   */
  private static boolean useRealMinScalingSpan() {
    return Boolean.parseBoolean(System.getProperty("robolectric.useRealMinScalingSpan", "true"));
  }

  /**
   * Reflection is needed to access internal Android dimen constants, which are static final ints,
   * so referencing them directly causes inlining. Note {@link AndroidInternalDimenReflector} is
   * designed to be temporary until the real {@link
   * android.view.ViewConfiguration#get(android.content.Context)} is called.
   */
  @ForType(com.android.internal.R.bool.class)
  interface AndroidInternalBoolReflector {
    @Static
    @Accessor("config_ui_enableFadingMarquee")
    int getEnableFadingMarquee();
  }
}
