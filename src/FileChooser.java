import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class FileChooser
{
    public static Class chooser (URL url, File f, Class c, URLClassLoader ucl, String str1, String str2, String str3, int index) throws MalformedURLException , ClassNotFoundException
    {
        while (c == null)
        {
            if (index == 0)
            {
                url = f.getParentFile().toURI().toURL();
                URL[] urla = {url};
                ucl = new URLClassLoader(urla);
                c = Class.forName(str1, true, ucl);
            }
            if (index == 1)
            {
                url = f.getParentFile().getParentFile().toURI().toURL();
                URL[] urlb = {url};
                ucl = new URLClassLoader(urlb);
                c = Class.forName((str2 + "." + str1), true, ucl);
            }
            if (index == 2)
            {
                url = f.getParentFile().getParentFile().getParentFile().toURI().toURL();
                URL[] urlc = {url};
                ucl = new URLClassLoader(urlc);
                c = Class.forName(str3 + "." + str2 + "." + str1, true, ucl);
            }
        }
        return c;
    }
}
