import java.io.*;
import java.util.Properties;
import java.nio.channels.FileChannel;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;
import org.apache.commons.io.FileUtils;
import gov.loc.repository.pairtree.Pairtree;

/**
 * HTRC FE Rsync Script Generator
 *
 * <P>Pre-requisite:  a collection.properties file</p>
 * collectionLocation=mylocation  -- the location of the list of volume ids
 * outputDir=myOutputDir -- the location of directory in which to put the output file
 * outputFile=myOutputFile  -- the name of the output file
 *
 */
public class Main
{
    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("collection.properties"));
        String location = properties.getProperty("collectionLocation");
        String OutputDir_path = PropertyManager.getProperty("outputDir");
        String result_name = PropertyManager.getProperty("outputFile");

        PrintWriter printWriter = new PrintWriter(result_name);
        printWriter.write("#!/bin/bash\n");

        BufferedReader br = new BufferedReader(new FileReader(location));
        br.readLine();//throw away the fist line, which is a header row

        String volumeId;
        while ((volumeId = br.readLine()) != null) {
            volumeId = URLDecoder.decode(volumeId, "UTF-8");

            Pairtree pt = new Pairtree();

            // Parse the volume ID
            String sourcePart = volumeId.substring(0, volumeId.indexOf("."));
            String volumePart = volumeId.substring(volumeId.indexOf(".") + 1, volumeId.length());

            String uncleanId = pt.uncleanId(volumePart);
            String path = pt.mapToPPath(uncleanId);
            String cleanVolumePart = pt.cleanId(volumePart);

            printWriter.write("rsync -v 'sandbox.htrc.illinois.edu::pd-features"
                    + File.separator + "advanced"
                    + File.separator + sourcePart
                    + File.separator + "pairtree_root"
                    + File.separator + path
                    + File.separator + cleanVolumePart
                    + File.separator + sourcePart
                    + "."
                    + cleanVolumePart
                    + ".advanced.json.bz2' $(pwd)\n");

            printWriter.write("rsync -v 'sandbox.htrc.illinois.edu::pd-features"
                    + File.separator + "basic"
                    + File.separator + sourcePart
                    + File.separator + "pairtree_root"
                    + File.separator + path
                    + File.separator + cleanVolumePart
                    + File.separator + sourcePart
                    + "."
                    + cleanVolumePart
                    + ".basic.json.bz2' $(pwd)\n");
        }

        printWriter.close();

        File final_result = new File(OutputDir_path, result_name);
        if (final_result.exists()) {
            final_result.delete();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(new File(result_name)).getChannel();
            destination = new FileOutputStream(final_result).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }

    }

}