import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class PropertyManager
{
    private static Properties properties = new Properties();

    static
    {
        try
        {
            properties.load(new FileInputStream("collection.properties"));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static String getProperty(String s)
    {
        return properties.getProperty(s);
    }
}