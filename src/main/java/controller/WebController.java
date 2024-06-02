package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import model.FileService;
import model.NeuralNetworkService;
import model.OntologyService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

public class WebController extends HttpServlet {

    protected FileService fileService = new FileService();
    protected NeuralNetworkService networkService = new NeuralNetworkService();
    protected OntologyService ontologyService = new OntologyService();
    protected String result = null;

    /**
     * true - только отмеченные radio не сохраняются
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //запрос пример
        // Установка типа содержимого ответа на 'application/json'
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        JsonObject jsonResponse = new JsonObject();
        PrintWriter out = resp.getWriter();

        // Получение BufferedReader для чтения тела запроса
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = req.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        // Преобразование собранной строки в JSON объект
        String request = sb.toString();
        // Создание экземпляра Gson
        Gson gson = new Gson();
        // Конвертация строки в JsonObject
        JsonObject jsonRequest = gson.fromJson(request, JsonObject.class);

        /*
        запрос 2 от клиента:
        startNetwork: true;
        ответ клиенту:
        200:
        networkResult: "3_21"
        500:
        error: true
        */
        if (jsonRequest.get("startNetwork") != null) {
            System.out.println("\t\t\ttest startNS");
            try {
                this.result = networkService.runNs(fileService.getFilesFromFoder("pt").getAbsolutePath(),
                        (fileService.checkFileByFolder("jpg") ? fileService.getFilesFromFoder("jpg") :
                                fileService.getFilesFromFoder("png")).getAbsolutePath());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            //test
//            this.result = "nullNamespace(weights=['C:\\\\Users\\\\Public\\\\diplomaNS\\\\uploads\\\\best5.pt'], source='C:\\\\Users\\\\Public\\\\diplomaNS\\\\uploads\\\\1.jpg', img_size=1280, conf_thres=0.25, iou_thres=0.45, device='', view_img=False, save_txt=False, save_conf=False, nosave=False, classes=None, agnostic_nms=False, augment=False, update=False, project='runs/detect', name='exp', exist_ok=False, no_trace=False)\n" +
//                    "Fusing layers...\n" +
//                    "IDetect.fuse\n" +
//                    "Convert model to Traced-model...\n" +
//                    "traced_script_module saved!\n" +
//                    "model is traced!\n" +
//                    "\n" +
//                    "1 3_21, Done. (1220.0ms) Inference, (2.9ms) NMS\n" +
//                    "The image with the result is saved in: runs\\detect\\exp7\\1.jpg\n" +
//                    "Done. (1.250s)";
        }

        //3
        if (this.result != null) {
            networkService.parseResultNs(this.result); //заполняется networkService.result
            if (networkService.getResult() != null) {
                jsonResponse.addProperty("networkResult", networkService.getResult());
                resp.setStatus(HttpServletResponse.SC_OK); //статус - ОК
            } else {
                jsonResponse.addProperty("error", "true");
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); //ошибка апи (проблема на сервере)
            }
        }

        /*
        запрос 3 от клиента:
        findObjectOntology: true
        objectOntology: value (одно из значений radios)
        ответ клиенту:
        200:
        "elements": [ - массив элементов из онтологии по выбранному radio
                "title",
                "title",
                "title"
              ],
        500:
        error: true
         */
        if (jsonRequest.get("findObjectOntology") != null
                && jsonRequest.get("objectOntology") != null) {
            String variantObjectOntology = jsonRequest.get("objectOntology").getAsString();
            List<String> objects = ontologyService.createRequestSPARQL(variantObjectOntology); //выполняется запрос к онтологии
            if (objects != null && !objects.isEmpty()) {
                JsonArray elements = new JsonArray();
                for(int index = 0; index < objects.size(); index++) {
                    elements.add(objects.get(index));
                }
                jsonResponse.add("elements", elements);
                resp.setStatus(HttpServletResponse.SC_OK); //статус - ОК
            } else {
                jsonResponse.addProperty("error", "true");
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); //ошибка апи (проблема на сервере)
            }
        }

        /*
        запрос 4 от клиента:
        findIndividualsOntology: true
        property: "title" (одно из названия свойства)
        ответ клиенту:
        200:
        "individuals": [ - массив индивидов из онтологии по выбранному свойству
                "title",
                "title",
                "title"
              ],
        500:
        error: true
         */
        if (jsonRequest.get("findIndividualsOntology") != null
                && jsonRequest.get("property") != null) {
            String property = jsonRequest.get("property").getAsString();

            String newRsource = "individuals";
            ontologyService.addRequest(newRsource, property, networkService.getResult()); //добавляю доп запрос
            List<String> individuals = ontologyService.createRequestSPARQL(newRsource);
            if (individuals != null && !individuals.isEmpty()) {
                JsonArray version = new JsonArray(); //экземпляры/индивиды
                for(int index = 0; index < individuals.size(); index++) {
                    version.add(individuals.get(index));
                }
                jsonResponse.add("individuals", version);
                resp.setStatus(HttpServletResponse.SC_OK); //статус - ОК
            } else {
                jsonResponse.addProperty("error", "true");
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); //ошибка апи (проблема на сервере)
            }
        }


        /*
        запрос 5 от клиента:
        findPropertyIndividual: true
        individual: "title" (одно из названия свойства)
        ответ клиенту:
        200:
        "properties": [ - массив свойств индивида
                "title",
                "title",
                "title"
              ],
        "values": [ - массив значений свойств
                "title",
                "title",
                "title"
              ],
        500:
        error: true
         */
        //6
        if (jsonRequest.get("findPropertyIndividual") != null
                && jsonRequest.get("individual") != null) {
            String individual = jsonRequest.get("individual").getAsString();

            List<List<String>> information = ontologyService.getInformationByIndividual(individual);
            if (information != null && !information.isEmpty()) {
                JsonArray properties = new JsonArray(); //свойсвта
                JsonArray values = new JsonArray(); //значения свойств

                for (int i = 0; i < information.get(0).size(); i++) {
                    properties.add(information.get(0).get(i));
                    values.add(information.get(1).get(i));
                }

                jsonResponse.add("properties", properties);
                jsonResponse.add("values", values);
                resp.setStatus(HttpServletResponse.SC_OK); //статус - ОК
            } else {
                jsonResponse.addProperty("error", "true");
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); //ошибка апи (проблема на сервере)
            }
        }

        System.out.println("responce\n:" + jsonRequest);
        out.println(jsonResponse); //отправка ответа json
        out.close();
    }

//    @Override
//    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        doGet(request, response);
//    }

}
