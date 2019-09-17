package uk.gov.pay.api.utils;

public class PathHelper {

    public static String getPathType(String pathValue, String method) {
        String path = pathValue;
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        if (path.endsWith("/capture")) {
            return "capture_payment";
        }

        if (path.endsWith("/payments") && method.equals("POST")) {
            return "create_payment";
        }

        return "";
    }
}
