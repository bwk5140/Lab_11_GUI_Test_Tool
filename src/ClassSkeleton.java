import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class ClassSkeleton {
    Class c;

    public ClassSkeleton(Class c) {
        this.c = c;
    }

    String checkModifier(int modifier) {
        String str = "";
        if ((modifier & 1) > 0) {
            str = str + "public ";
        } else if ((modifier & 4) > 0) {
            str = str + "protected ";
        } else if ((modifier & 2) > 0) {
            str = str + "private ";
        }

        if ((modifier & 1024) > 0) {
            str = str + "abstract ";
        } else if ((modifier & 8) > 0) {
            str = str + "static ";
        }

        if ((modifier & 16) > 0) {
            str = str + "final ";
        }

        return str;
    }

    String[] getSkeleton(Hashtable ht) {
        String context = "";
        String rowHeader = "";
        String[] retArr = new String[2];
        if (this.c != null) {
            Package p = this.c.getPackage();
            context = context + p.toString() + ";\n\n";
            rowHeader = rowHeader + "\n\n";
            context = context + this.checkModifier(this.c.getModifiers());
            context = context + "class " + this.getToken(this.c.getName()) + " {\n";
            rowHeader = rowHeader + "\n";
            Field[] fds = this.c.getDeclaredFields();
            context = context + "  // Fields\n";
            rowHeader = rowHeader + "\n";

            for(int i = 0; i < fds.length; ++i) {
                context = context + "  " + this.checkModifier(fds[i].getModifiers());
                context = context + this.getToken(fds[i].getType().getName()) + " ";
                context = context + fds[i].getName() + ";\n";
                rowHeader = rowHeader + "\n";
            }

            context = context + "\n";
            rowHeader = rowHeader + "\n";
            Constructor[] cst = this.c.getDeclaredConstructors();
            context = context + "  // Constructors\n";
            rowHeader = rowHeader + "\n";

            for(int i = 0; i < cst.length; ++i) {
                String key = cst[i].toString();
                key = key.substring(key.lastIndexOf(" ") + 1);
                key = key.replaceFirst(cst[i].getName(), cst[i].getName() + ".<init>");
                key = key.replaceAll(",", ", ");
                Integer curRuns = (Integer)ht.get(key);
                if (curRuns != null) {
                    rowHeader = rowHeader + curRuns + "\n";
                } else {
                    rowHeader = rowHeader + "0\n";
                }

                context = context + "  " + this.checkModifier(cst[i].getModifiers());
                context = context + this.getToken(this.c.getName());
                Class[] ca = cst[i].getParameterTypes();
                context = context + "(";

                for(int j = 0; j < ca.length; ++j) {
                    Class carray = ca[j].getComponentType();
                    String str = "";
                    if (carray == null) {
                        str = this.getToken(ca[j].getName());
                    } else {
                        str = this.getToken(carray.getName()) + "[]";
                    }

                    context = context + str;
                    if (j < ca.length - 1) {
                        context = context + ", ";
                    }
                }

                context = context + ") { }\n";
            }

            context = context + "\n";
            rowHeader = rowHeader + "\n";
            Method[] md = this.c.getDeclaredMethods();
            context = context + "  // Methods\n";
            rowHeader = rowHeader + "\n";

            for(int i = 0; i < md.length; ++i) {
                String key = md[i].toString();
                key = key.substring(key.lastIndexOf(" ") + 1);
                key = key.replaceAll(",", ", ");
                Integer curRuns = (Integer)ht.get(key);
                if (curRuns != null) {
                    rowHeader = rowHeader + curRuns;
                } else {
                    rowHeader = rowHeader + "0";
                }

                context = context + "  " + this.checkModifier(md[i].getModifiers());
                context = context + this.getToken(md[i].getReturnType().getName()) + " ";
                context = context + md[i].getName();
                Class[] ca = md[i].getParameterTypes();
                context = context + "(";

                for(int j = 0; j < ca.length; ++j) {
                    Class carray = ca[j].getComponentType();
                    String str = "";
                    if (carray == null) {
                        str = this.getToken(ca[j].getName());
                    } else {
                        str = this.getToken(carray.getName()) + "[]";
                    }

                    context = context + str;
                    if (j < ca.length - 1) {
                        context = context + ", ";
                    }
                }

                context = context + ")\n";
                rowHeader = rowHeader + "\n";
            }

            context = context + "}\n\n";
            rowHeader = rowHeader + "\n\n";
        }

        retArr[0] = context;
        retArr[1] = rowHeader;
        return retArr;
    }

    String[] getSkeleton() {
        String context = "";
        String rowHeader = "";
        String[] retArr = new String[2];
        if (this.c != null) {
            Package p = this.c.getPackage();
            context = context + p.toString() + ";\n\n";
            rowHeader = rowHeader + "\n\n";
            context = context + this.checkModifier(this.c.getModifiers());
            context = context + "class " + this.getToken(this.c.getName()) + " {\n";
            rowHeader = rowHeader + "\n";
            Field[] fds = this.c.getDeclaredFields();
            context = context + "  // Fields\n";
            rowHeader = rowHeader + "\n";

            for(int i = 0; i < fds.length; ++i) {
                context = context + "  " + this.checkModifier(fds[i].getModifiers());
                context = context + this.getToken(fds[i].getType().getName()) + " ";
                context = context + fds[i].getName() + ";\n";
                rowHeader = rowHeader + "\n";
            }

            context = context + "\n";
            rowHeader = rowHeader + "\n";
            Constructor[] cst = this.c.getDeclaredConstructors();
            context = context + "  // Constructors\n";
            rowHeader = rowHeader + "\n";

            for(int i = 0; i < cst.length; ++i) {
                context = context + "  " + this.checkModifier(cst[i].getModifiers());
                context = context + this.getToken(this.c.getName()) + "() { }\n";
                rowHeader = rowHeader + "0\n";
            }

            context = context + "\n";
            rowHeader = rowHeader + "\n";
            Method[] md = this.c.getDeclaredMethods();
            context = context + "  // Methods\n";
            rowHeader = rowHeader + "\n";

            for(int i = 0; i < md.length; ++i) {
                context = context + "  " + this.checkModifier(md[i].getModifiers());
                context = context + this.getToken(md[i].getReturnType().getName()) + " ";
                context = context + md[i].getName();
                Class[] ca = md[i].getParameterTypes();
                context = context + "(";

                for(int j = 0; j < ca.length; ++j) {
                    Class carray = ca[j].getComponentType();
                    String str = "";
                    if (carray == null) {
                        str = this.getToken(ca[j].getName());
                    } else {
                        str = this.getToken(carray.getName()) + "[]";
                    }

                    context = context + str;
                    if (j < ca.length - 1) {
                        context = context + ", ";
                    }
                }

                context = context + ")\n";
                rowHeader = rowHeader + "0\n";
            }

            context = context + "}\n\n";
            rowHeader = rowHeader + "\n\n";
        }

        retArr[0] = context;
        retArr[1] = rowHeader;
        return retArr;
    }

    String getToken(String str) {
        StringTokenizer st = new StringTokenizer(str, ".");

        for(str = ""; st.hasMoreTokens(); str = st.nextToken()) {
        }

        return str;
    }
}