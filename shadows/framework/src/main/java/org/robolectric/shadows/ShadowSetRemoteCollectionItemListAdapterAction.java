package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/**
 * The action {@code RemoteViews.setRemoteAdapter(int, Intent)} records when the framework converts
 * a collection's {@code RemoteViewsService} into items. On a device {@code AppWidgetManager} fills
 * the items in from the service before the views reach the host; here they are collected from the
 * service in-process when the action is applied without them, as {@link ShadowRemoteViewsAdapter}
 * drives the service for an unconverted collection.
 */
@Implements(
    className = "android.widget.RemoteViews$SetRemoteCollectionItemListAdapterAction",
    isInAndroidSdk = false,
    minSdk = VANILLA_ICE_CREAM)
public class ShadowSetRemoteCollectionItemListAdapterAction {

  @RealObject private Object realAction;

  /** Collects the service's items when the action has only its intent, then applies them. */
  @Implementation
  protected void apply(View root, ViewGroup rootParent, RemoteViews.ActionApplyParams params) {
    ActionReflector action = reflector(ActionReflector.class, realAction);
    Intent serviceIntent = action.getServiceIntent();
    if (action.getItems() == null && serviceIntent != null) {
      RemoteViews.RemoteCollectionItems items =
          (RemoteViews.RemoteCollectionItems) ShadowRemoteViewsAdapter.collectItems(serviceIntent);
      if (items != null) {
        action.setItems(items);
      }
    }
    action.apply(root, rootParent, params);
  }

  @ForType(className = "android.widget.RemoteViews$SetRemoteCollectionItemListAdapterAction")
  interface ActionReflector {
    @Accessor("mItems")
    RemoteViews.RemoteCollectionItems getItems();

    @Accessor("mItems")
    void setItems(RemoteViews.RemoteCollectionItems items);

    @Accessor("mServiceIntent")
    Intent getServiceIntent();

    @Direct
    void apply(View root, ViewGroup rootParent, RemoteViews.ActionApplyParams params);
  }
}
