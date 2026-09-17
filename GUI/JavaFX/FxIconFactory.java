package GUI.JavaFX;

import javafx.scene.shape.SVGPath;

/**
 * Bộ icon SVG dùng chung cho Sidebar và Topbar JavaFX.
 * Không phụ thuộc emoji/font của hệ điều hành nên icon hiển thị đồng nhất.
 */
public final class FxIconFactory {

    private FxIconFactory() {
    }

    public static SVGPath create(String name) {
        return create(name, 0.82);
    }

    public static SVGPath create(String name, double scale) {
        SVGPath path = new SVGPath();
        path.setContent(pathFor(name));
        path.setScaleX(scale);
        path.setScaleY(scale);
        return path;
    }

    public static String pathFor(String name) {
        switch (name) {
            case "home":
                return "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z";
            case "apps":
                return "M3 3h7v7H3V3m11 0h7v7h-7V3M3 14h7v7H3v-7m11 0h7v7h-7v-7";
            case "category":
                return "M4 4h7v7H4V4m9 0h7v7h-7V4M4 13h7v7H4v-7m9 0h7v7h-7v-7";
            case "truck":
                return "M3 5h11v9h2.2l1.8-3h3l2 3v4h-2a3 3 0 0 1-6 0H9a3 3 0 0 1-6 0H1V7h2V5m3 11a1 1 0 1 0 0 2 1 1 0 0 0 0-2m12 0a1 1 0 1 0 0 2 1 1 0 0 0 0-2m-2-3h4.6l-1-2H18l-2 2";
            case "gift":
                return "M20 12v8H4v-8H2V7h5.1A3 3 0 0 1 12 3.6 3 3 0 0 1 16.9 7H22v5h-2M9 7h2V5.8C10.5 4.6 9 4 8 4a2 2 0 0 0-2 2c0 .6.4 1 1 1h2m4 0h2c.6 0 1-.4 1-1a2 2 0 0 0-2-2c-1 0-2.5.6-3 1.8V7h2M4 9v1h7V9H4m9 0v1h7V9h-7m-7 3v6h5v-6H6m7 0v6h5v-6h-5";
            case "person":
                return "M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8m-7 8c0-4 3-6 7-6s7 2 7 6H5";
            case "group":
                return "M9 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8m6-1a3 3 0 1 0 0-6 3 3 0 0 0 0 6M2 21c0-4 3-7 7-7s7 3 7 7H2m14-7c3.5.3 6 2.5 6 6h-4c0-2.6-1-4.8-2.7-6.2 1.1-.5 2.2-.8 3.7-.8";
            case "shield":
                return "M12 2l8 3v6c0 5-3.4 9.7-8 11-4.6-1.3-8-6-8-11V5l8-3m0 4L7 8v3c0 3.5 2 6.6 5 7.8 3-1.2 5-4.3 5-7.8V8l-5-2";
            case "package":
                return "M12 2l9 5-9 5-9-5 9-5m-9 7l8 4.5V22l-8-4.5V9m18 0l-8 4.5V22l8-4.5V9";
            case "store":
                return "M4 4h16l2 5a3 3 0 0 1-5 2 3 3 0 0 1-5 0 3 3 0 0 1-5 0 3 3 0 0 1-5-2l2-5m1 9v7h14v-7M9 20v-5h6v5";
            case "warehouse":
                return "M3 9l9-6 9 6v12H3V9m4 2v2h10v-2H7m0 4v2h10v-2H7m0 4v2h10v-2H7";
            case "receipt":
                return "M6 2h12v20l-3-2-3 2-3-2-3 2V2m3 5h6v2H9V7m0 4h6v2H9v-2m0 4h6v2H9v-2";
            case "cart":
                return "M3 4h2l2.4 10h9.7l2-7H7m2 12a2 2 0 1 0 0 4 2 2 0 0 0 0-4m8 0a2 2 0 1 0 0 4 2 2 0 0 0 0-4";
            case "bag":
                return "M6 7V5a6 6 0 0 1 12 0v2h3v15H3V7h3m3 0h6V5a3 3 0 0 0-6 0v2";
            case "history":
                return "M13 3a9 9 0 1 1-8.5 6H2l3.5-3.5L9 9H6.5A6.5 6.5 0 1 0 13 5.5V3m-1 5h2v5l4 2-1 1.7-5-2.7V8";
            case "user-settings":
                return "M10 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8m-7 9c0-4 3-7 7-7 1.2 0 2.3.3 3.3.7A6 6 0 0 0 13 16c0 1.5.5 2.9 1.4 4H3m14-8l1.2 1.2 1.7-.5.8 1.5-1.2 1.3.2 1.8-1.6.8-1-1.4-1.8-.2-.5-1.7 1.5-.9.2-1.8-1.7-.5-.5-1.7 1.4-1 .2-1.8 1.7-.5";
            case "account":
                return "M12 2a5 5 0 1 0 0 10 5 5 0 0 0 0-10m-9 20c0-5 4-8 9-8s9 3 9 8H3";
            case "lock":
                return "M6 10V7a6 6 0 0 1 12 0v3h2v12H4V10h2m3 0h6V7a3 3 0 0 0-6 0v3m3 4a2 2 0 0 0-1 3.7V20h2v-2.3A2 2 0 0 0 12 14";
            case "chart":
                return "M4 20V10h4v10H4m6 0V4h4v16h-4m6 0v-7h4v7h-4";
            case "bar-chart":
                return "M3 21V3h2v16h16v2H3m5-4v-5h3v5H8m5 0V7h3v10h-3m5 0V4h3v13h-3";
            case "logout":
                return "M10 3H4v18h6v-2H6V5h4V3m4 4l5 5-5 5v-3h-6v-4h6V7";
            case "collapse":
                return "M14.5 6L9 12l5.5 6 1.5-1.4-4.2-4.6 4.2-4.6L14.5 6";
            case "menu":
                return "M3 6h18v2H3V6m0 5h18v2H3v-2m0 5h18v2H3v-2";
            case "chevron-right":
                return "M9 6l6 6-6 6 1.5 1.5L18 12 10.5 4.5 9 6";
            case "check":
                return "M9 16.2L4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4L9 16.2";
            default:
                return "M4 4h16v16H4V4";
        }
    }
}
