package controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet(name = "ImageUploadServlet", urlPatterns = {"/upload"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 5 * 1024 * 1024)
public class ImageUploadServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Part filePart = request.getPart("file");
        
        if (filePart != null) {
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            
            String applicationPath = getServletContext().getRealPath("");
            String newApplicationPath = applicationPath.replace("build/web", "web") + File.separator + "profilepicture";

            // Create folder if it does not exist
            File uploadDir = new File(newApplicationPath);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            File file1 = new File(uploadDir, fileName);
            InputStream inputStream1 = filePart.getInputStream();
            Files.copy(inputStream1, file1.toPath(), StandardCopyOption.REPLACE_EXISTING);

            response.getWriter().write("Upload successful");
        } else {
            response.getWriter().write("Upload failed");
        }
    }
}

