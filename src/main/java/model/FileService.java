package model;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.*;

/*
* В этот момент оно будет пустым, если вы уже (неявно) проанализировали тело запроса заранее. Тело HTTP-запроса может быть прочитано/анализировано только один раз (поскольку клиент отправил его только один раз и не будет отправлять его несколько раз).

Тело запроса будет неявно прочитано/анализировано, если вы вызовете любой из следующих методов перед передачей запроса в Commons FileUpload:

request.getParameter();
request.getParameterMap();
request.getParameterNames();
request.getParameterValues();
request.getReader();
request.getInputStream();
Вам необходимо заранее убедиться, что вы не вызываете ни один из этих методов (также проверьте все фильтры сервлетов).
* */

/**
 * сохранение файлов для последующего распозанвания
 */
public class FileService {
    protected final int fileMaxSize = 100 * 140000;
    protected final int memMaxSize = 100 * 140000;
//    protected String filePath = "C:\\Users\\Public\\diplomaNS\\uploads\\";
//    protected String filePath = "/usr/local/tomcat/"; //true
    protected String filePath = "/home/alina/uploads/";
    protected File file;
    protected List<File> files = new ArrayList<>();
    protected List<File> errors = new ArrayList<>();

    public List<File> getFiles() {
        return files;
    }

    public List<File> getErrors() {
        return errors;
    }

    /***
     * метод по загрузке файлов в сервис (в папку на сервисе) для работы НС
     * @param request
     * @return корректность возврата; true - файлы загружены, false - не загружены
     */
    public boolean uploadFile(HttpServletRequest request) {
        System.out.println("start uploadFile");
        boolean checkUploadFile = false;
        DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
        diskFileItemFactory.setRepository(new File(filePath));
        diskFileItemFactory.setSizeThreshold(memMaxSize);

        ServletFileUpload upload = new ServletFileUpload(diskFileItemFactory);
        upload.setSizeMax(fileMaxSize);
        try {
            List fileItems = upload.parseRequest(request);
            Iterator iterator = fileItems.iterator();
            while (iterator.hasNext()) {
                FileItem fileItem = (FileItem) iterator.next();
                if (!fileItem.isFormField()) {
                    String fileName = fileItem.getName();
                    if (fileName.lastIndexOf("\\") >= 0) {
                        file = new File(filePath +
                                fileName.substring(fileName.lastIndexOf("\\")));
                    } else {
                        file = new File(filePath +
                                fileName.substring(fileName.lastIndexOf("\\") + 1));
                    }
                    fileItem.write(file);
                    System.out.println("\t" + fileName + " is uploaded to " + filePath);
                    //проверка на формат файлов
                    if(file.getName().contains(".rdf") ||
                            file.getName().contains(".jpg") || file.getName().contains(".png") ||
                            file.getName().contains(".pt")) {
                        files.add(file);
                    } else errors.add(file);
                }
            }
            if (files != null && files.size() == 3) checkUploadFile = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("end uploadFile");
        return checkUploadFile;
    }

    /**
     * зависимость файла от его расширения (формата)
     * ключ - формат файла
     * значение - файл
     * @param files
     * @return набор ключ-значение
     */
    public Map<String, File> getFilesByFormat (List<File> files) {
        System.out.println("start getFilesByFormat");
        Map<String, File> filesByFormat = new HashMap<>();
        for (File file : files) {
            filesByFormat.put(getFormatFile(file), file);
        }
        System.out.println("end getFilesByFormat");
        return filesByFormat;
    }

    /**
     * метод для получения формата файла
     * @param file файл
     * @return
     */
    public String getFormatFile (File file) {
        return file.getName().split("\\.")[1];
    }

    /**
     * Метод получает загруженные в папку файлы по пути filePath и записывает файлы в files
     */
    public void addFilesFromFolder() { //add
        System.out.println("start addFilesFromFolder --> \n");
        File folder = new File(filePath);
        File[] files = folder.listFiles(); //метод listFiles() для получения массива объектов File для всех файлов и папок в папке.
        for (File file : files) {
            if (file.isFile() &&
                    (file.getName().contains("jpg") || file.getName().contains("png") ||
                            file.getName().contains("pt") || file.getName().contains("rdf"))) {
                System.out.println("\t" + file.getName() + " is readed from " + filePath);
                this.files.add(file);
            }
        }
        System.out.println("end addFilesFromFolder");
    }

    /**
     * возвращает файл из папки по формату
     * @param format
     * @return
     */
    public File getFilesFromFoder(String format) {
        File file = null;
        if (files.isEmpty())
            addFilesFromFolder(); //беру из папки загруженные файлы и записываю в files
        for (File fileFolder : files) {
            if (fileFolder.getName().contains(format)) file = fileFolder;
        }
        return file;
    }

    /**
     * проверяет есть ли файл в папке с таким форматом
     * @param format
     * @return
     */
    public boolean checkFileByFolder(String format) {
        return getFilesFromFoder(format) != null;
    }

}
