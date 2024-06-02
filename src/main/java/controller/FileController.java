package controller;

import com.google.gson.JsonObject;
import model.FileService;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * запрос 1 от клиента:
 * multipart/form-data с файлами
 * ответ клиенту:
 * 200:
 * формат: файл - сохраненные
 * 500:
 * формат: файл - ошибка формата
 */
@MultipartConfig //чтобы использовать getPart/s (и getParameter ?) при форме multipart/form-data
public class FileController extends HttpServlet {

    protected FileService fileService = new FileService();

    /**
     * метод получает пост запрос от клиента для загрузки файлов
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("fileUpload post request");
        JsonObject jsonResponse = new JsonObject();
        PrintWriter out = resp.getWriter();
        if(fileService.uploadFile(req)) { //чтение результатов из файла
            for (File file : fileService.getFiles()) {
                jsonResponse.addProperty(fileService.getFormatFile(file), file.getName());
            }
            out.print(jsonResponse);
        } else if (!fileService.getErrors().isEmpty()) {
            jsonResponse.addProperty("error", "true"); //показатель, что есть ошибки
            for (File file : fileService.getErrors()) {
                jsonResponse.addProperty(fileService.getFormatFile(file), file.getName());
            }
            out.print(jsonResponse);
        } else resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); //ошибка апи (проблема на сервере)
        resp.setStatus(HttpServletResponse.SC_OK); //статус - ОК
        out.close();
    }

}
