package app.revanced.integrations.patches.theme;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.ReVancedUtils;

public final class ThemePatch {
    private static final int seekbarPrimaryScale = 3;
    private static final int seekbarBufferedScale = 1;
    private static final int seekbarBufferedColor = 0xCC00CC;
    private static final int seekbarUnbufferedScale = 1;
    private static final int seekbarUnbufferedColor = 0x00CC00;
    private static final int ORIGINAL_SEEKBAR_CLICKED_COLOR = -65536;
    private static final int[] rainbowColors = new int[] {
            0xFFFF0018, // red
            0xFFFFA52C, // orange
            0xFFFFFF41, // yellow
            0xFF008018, // green
            0xFF0000F9, // blue
            0xFF86007D // violet
    };

    private static void resetSeekbarColor() {
        ReVancedUtils.showToastShort("Invalid seekbar color value. Using default value.");
        SettingsEnum.SEEKBAR_COLOR.saveValue(SettingsEnum.SEEKBAR_COLOR.defaultValue);
    }

    /**
     * Injection point.
     */
    public static int getSeekbarClickedColorValue(final int colorValue) {
        // YouTube uses a specific color when the seekbar is clicked. Override in that case.
        return colorValue == ORIGINAL_SEEKBAR_CLICKED_COLOR ? getSeekbarColorValue() : colorValue;
    }

    public static int getSeekbarColorValue() {
        try {
            return Color.parseColor(SettingsEnum.SEEKBAR_COLOR.getString());
        } catch (Exception exception) {
            resetSeekbarColor();
            return getSeekbarColorValue();
        }
    }

    private static void drawSeekbarPrimary(Canvas canvas, RectF rect, int alpha) {
        scaleRect(rect, seekbarPrimaryScale);

        drawStripes(canvas, rect, alpha, rainbowColors);
    }

    private static void drawSeekbarInactivePrimary(Canvas canvas, RectF rect, int alpha) {
        scaleRect(rect, seekbarPrimaryScale);

        drawStripes(canvas, rect, alpha, rainbowColors);
    }

    private static void drawSeekbarBuffered(Canvas canvas, RectF rect, int alpha) {
        scaleRect(rect, seekbarBufferedScale);

        drawSolid(canvas, rect, alpha, seekbarBufferedColor);
    }

    private static void drawSeekbarUnbuffered(Canvas canvas, RectF rect, int alpha) {
        scaleRect(rect, seekbarUnbufferedScale);

        drawSolid(canvas, rect, alpha, seekbarUnbufferedColor);
    }

    private static void drawSolid(Canvas canvas, RectF rect, int alpha, int color){
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAlpha(alpha);

        canvas.drawRect(rect, paint);
    }

    private static void drawStripes(Canvas canvas, RectF rect, int alpha, int[] colors) {
        float stripeSize = rect.height() / colors.length;

        for (int i = 0; i < colors.length; i++) {
            int color = colors[i];

            drawSolid(canvas, new RectF(rect.left, rect.top + (i * stripeSize), rect.right, (rect.bottom - ((colors.length - (i + 1)) * stripeSize))), alpha, color);
        }
    }

    private static void scaleRect(RectF rect, int scale) {
        float height = rect.height() * (scale - 1) / 2;

        rect.top -= height;
        rect.bottom += height;
    }

    public static void splitSeekbarInactiveSegmentDrawHook(Canvas canvas, float left, float top, float right, float bottom, Paint paint) {
        splitSeekbarActiveSegmentDrawHook(canvas, left, top, right, bottom, paint);
    }

    public static void splitSeekbarActiveSegmentDrawHook(Canvas canvas, float left, float top, float right, float bottom, Paint paint) {
        RectF rect = new RectF(left, top, right, bottom);

        switch (paint.getColor() & 0xFFFFFF) {
            case 0xFF0000:
                drawSeekbarPrimary(canvas, rect, paint.getAlpha());
                break;
            case 0xAAAAAA:
                drawSeekbarBuffered(canvas, rect, paint.getAlpha());
                break;
            case 0x717171:
                drawSeekbarUnbuffered(canvas, rect, paint.getAlpha());
                break;
            default:
                canvas.drawRect(left, top, right, bottom, paint);
                break;
        }
    }

    public static void linearSeekbarPrimaryDrawHook(Canvas canvas, Rect rect, Paint paint) {
        drawSeekbarPrimary(canvas, new RectF(rect.left, rect.top, rect.right, rect.bottom), paint.getAlpha());
    }

    public static void linearSeekbarInactivePrimaryDrawHook(Canvas canvas, Rect rect, Paint paint) {
        drawSeekbarInactivePrimary(canvas, new RectF(rect.left, rect.top, rect.right, rect.bottom), paint.getAlpha());
    }

    public static void linearSeekbarUnbufferedDrawHook(Canvas canvas, Rect rect, Paint paint) {
        drawSeekbarUnbuffered(canvas, new RectF(rect.left, rect.top, rect.right, rect.bottom), paint.getAlpha());
    }

    public static void linearSeekbarActiveBufferedDrawHook(Canvas canvas, Rect rect, Paint paint) {
        drawSeekbarBuffered(canvas, new RectF(rect.left, rect.top, rect.right, rect.bottom), paint.getAlpha());
    }

    public static void linearSeekbarInactiveBufferedDrawHook(Canvas canvas, Rect rect, Paint paint) {
        drawSeekbarBuffered(canvas, new RectF(rect.left, rect.top, rect.right, rect.bottom), paint.getAlpha());
    }
}
