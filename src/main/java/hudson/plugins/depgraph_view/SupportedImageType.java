package hudson.plugins.depgraph_view;

/**
* @author wolfs
*/ // Data Structure to encode the content type and the -T argument for the graphviz tools
enum SupportedImageType {
    PNG("image/png", "png"),
    SVG("image/svg", "svg"),
    MAP("image/cmapx", "cmapx"),
    JSON("text/plain", "json", false),
    GV("text/plain", "gv", false);

    final String contentType;
    final String dotType;
    final boolean requiresProcessing;

    SupportedImageType(String contentType,
                       String dotType,
                       boolean requiresProcessing) {
        this.contentType = contentType;
        this.dotType = dotType;
        this.requiresProcessing = requiresProcessing;
    }

    SupportedImageType(String contentType,
                       String dotType) {
        this(contentType, dotType, true);
    }

}
