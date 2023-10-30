package dev.blocky.app.vx.windows.api;

public enum DwmAttribute
{
    DWMSBT_DISABLE(1),

    DWMSBT_MAINWINDOW(2),

    DWMSBT_TABBEDWINDOW(4),

    DWMSBT_TRANSIENTWINDOW(3),

    DWMWA_BORDER_COLOR(34),

    DWMWA_CAPTION_COLOR(35),

    DWMWA_MICA_EFFECT(1029),

    DWMWA_SYSTEMBACKDROP_TYPE(38),

    DWMWA_TEXT_COLOR(36),

    DWMWA_USE_IMMERSIVE_DARK_MODE(20);

    public final int value;

    DwmAttribute(int value)
    {
        this.value = value;
    }
}
