package com.a65apps.mapkitclustering;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;

import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.PlacemarkMapObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class ClusterAnimator {
    @NonNull
    public static ValueAnimator pointToBunch(final @NonNull List<PlacemarkMapObject> objects, @NonNull final List<Point> to) {
        if (objects.isEmpty() || to.size() != objects.size()) {
            throw new IllegalArgumentException("Wrong objects or points count");
        }
        Point clusterPoint = calcCenter(to);
        return pointToBunch(objects, to, clusterPoint);
    }

    @NonNull
    public static ValueAnimator pointToBunch(final @NonNull List<PlacemarkMapObject> objects, @NonNull final List<Point> to,
            @NonNull final Point clusterPoint) {
        if (objects.isEmpty() || to.size() != objects.size()) {
            throw new IllegalArgumentException("Wrong objects or points count");
        }

        final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        final ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float factor = (float) animation.getAnimatedValue();
                double kx, ky, lat, lon;
                for (int i = 0; i < objects.size(); i++) {
                    PlacemarkMapObject object = objects.get(i);
                    Point point = to.get(i);
                    kx = point.getLatitude() - clusterPoint.getLatitude();
                    ky = point.getLongitude() - clusterPoint.getLongitude();
                    lat = clusterPoint.getLatitude() + (factor * kx);
                    lon = clusterPoint.getLongitude() + (factor * ky);
                    updateObjectGeometry(object, lat, lon);
                }
            }
        };
        animator.addUpdateListener(updateListener);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animator.removeUpdateListener(updateListener);
                animation.removeListener(this);
            }
        });
        return animator;
    }

    @NonNull
    public static ValueAnimator bunchToPoint(@NonNull final List<PlacemarkMapObject> objects, @NonNull Point to) {
        if (objects.isEmpty()) {
            throw new IllegalArgumentException("Empty objects");
        }

        final List<Point> startPoint = new ArrayList<>(objects.size());
        for (PlacemarkMapObject placemarkMapObject : objects) {
            startPoint.add(placemarkMapObject.getGeometry());
        }

        final double targetX = to.getLatitude();
        final double targetY = to.getLongitude();

        final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        final ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float factor = (float) animation.getAnimatedValue();
                double kx, ky, lat, lon;
                for (int i = 0; i < objects.size(); i++) {
                    PlacemarkMapObject object = objects.get(i);
                    Point start = startPoint.get(i);
                    kx = targetX - start.getLatitude();
                    ky = targetY - start.getLongitude();
                    lat = start.getLatitude() + (factor * kx);
                    lon = start.getLongitude() + (factor * ky);
                    updateObjectGeometry(object, lat, lon);
                }
            }
        };
        animator.addUpdateListener(updateListener);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animator.removeUpdateListener(updateListener);
                animation.removeListener(this);
            }
        });
        return animator;
    }

    @NonNull
    public static ValueAnimator pointToPoint(@NonNull final PlacemarkMapObject placemarkMapObject, @NonNull Point to) {
        Point from = placemarkMapObject.getGeometry();
        final double dX = to.getLatitude() - from.getLatitude();
        final double dY = to.getLongitude() - from.getLongitude();

        final double startX = from.getLatitude();
        final double startY = from.getLongitude();

        final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        final ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float factor = (float) animation.getAnimatedValue();
                double lat = startX + (factor * dX);
                double lon = startY + (factor * dY);
                updateObjectGeometry(placemarkMapObject, lat, lon);
            }
        };
        animator.addUpdateListener(updateListener);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animator.removeUpdateListener(updateListener);
                animation.removeListener(this);
            }
        });
        return animator;
    }

    public static Point calcCenter(@NonNull List<Point> points) {
        int length = points.size();
        double x = 0, y = 0, z = 0;
        for (Point point : points) {
            double lat = point.getLatitude();
            double lng = point.getLongitude();
            lat = Math.toRadians(lat);
            lng = Math.toRadians(lng);
            x += Math.cos(lat) * Math.cos(lng);
            y += Math.cos(lat) * Math.sin(lng);
            z += Math.sin(lat);
        }

        x /= length;
        y /= length;
        z /= length;

        double longitude = Math.atan2(y, x);
        double hypot = Math.hypot(x, y);
        double latitude = Math.atan2(z, hypot);

        latitude = Math.toDegrees(latitude);
        longitude = Math.toDegrees(longitude);
        return new Point(latitude, longitude);
    }

    private static void updateObjectGeometry(@NonNull PlacemarkMapObject placemarkMapObject, double lat, double lon) {
        try {
            placemarkMapObject.setGeometry(new Point(lat, lon));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}