package GeneralElements.accessories;

import Applications.ItemMovementsApp;

/**
 * Created by mviswanathan on 13-05-2017.
 */
public class JetsAndSeekers {
    public enum JetTableColType {
        SLNO("SlNo."),
        ELEMENT("Element Type"),
        DETAILS("Details");

        private final String typeName;

        JetTableColType(String typeName) {
            this.typeName = typeName;
        }

        public String getValue() {
            return typeName;
        }

        @Override
        public String toString() {
            return typeName;
        }

        public static JetTableColType getEnum(String text) {
            JetTableColType retVal = null;
            if (text != null) {
                for (JetTableColType b : JetTableColType.values()) {
                    if (text.equalsIgnoreCase(b.typeName)) {
                        retVal = b;
                        break;
                    }
                }
            }
            return retVal;
        }
    }

    public enum ElementType {
        JET("Jet"),
        SEEKER("Seeker");

        private final String typeName;

        ElementType(String typeName) {
            this.typeName = typeName;
        }

        public String getValue() {
            return typeName;
        }

        @Override
        public String toString() {
            return typeName;
        }

        public static ElementType getEnum(String text) {
            ElementType retVal = null;
            if (text != null) {
                for (ElementType b : ElementType.values()) {
                    if (text.equalsIgnoreCase(b.typeName)) {
                        retVal = b;
                        break;
                    }
                }
            }
            return retVal;
        }
    }

    ElementType elementType;
    String name;

    void showError(String msg) {
        ItemMovementsApp.showError("Jet: " + msg);
    }
}
