package model;

import org.eclipse.jgit.api.Git;

import java.io.*;


public class NeuralNetworkService {

    protected String result = null;

    public String getResult() {
        return result;
    }

    /**
     * Метод выполняет запуск модели НС для распознавания на локальной машине - создает подпроцесс (как в консоли)
     * возможно это будет на отельной машине - запуск НС
     * @param pathNs - путь до файла модели НС
     * @param pathImg - путь до изображения
     * @throws IOException
     * @throws InterruptedException
     */
    public String runNs(String pathNs, String pathImg) throws IOException, InterruptedException {
        System.out.println("start runNs");
//        Thread.sleep(7000);
//        String result = "this.result = \"nullNamespace(weights=['C:\\\\\\\\Users\\\\\\\\Public\\\\\\\\diplomaNS\\\\\\\\uploads\\\\\\\\best5.pt'], source='C:\\\\\\\\Users\\\\\\\\Public\\\\\\\\diplomaNS\\\\\\\\uploads\\\\\\\\1.jpg', img_size=1280, conf_thres=0.25, iou_thres=0.45, device='', view_img=False, save_txt=False, save_conf=False, nosave=False, classes=None, agnostic_nms=False, augment=False, update=False, project='runs/detect', name='exp', exist_ok=False, no_trace=False)\\n\" +\n" +
//                "                    \"Fusing layers...\\n\" +\n" +
//                "                    \"IDetect.fuse\\n\" +\n" +
//                "                    \"Convert model to Traced-model...\\n\" +\n" +
//                "                    \"traced_script_module saved!\\n\" +\n" +
//                "                    \"model is traced!\\n\" +\n" +
//                "                    \"\\n\" +\n" +
//                "                    \"1 3_21, Done. (1220.0ms) Inference, (2.9ms) NMS\\n\" +\n" +
//                "                    \"The image with the result is saved in: runs\\\\detect\\\\exp7\\\\1.jpg\\n\" +\n" +
//                "                    \"Done. (1.250s)\";";


//        String pythonScript =  "C:\\Users\\Public\\diplomaNS\\modelNS\\yolov7\\detect.py"; //true
        String pythonScript =  "/home/alina/yolov7/detect.py";
        System.out.println("test 1");
        ProcessBuilder processBuilder = new ProcessBuilder("python", pythonScript,
                "--weights", pathNs,
                "--source", pathImg,
                "--img", "1280");
        System.out.println("test 2 --> " + pathNs + " ; " + pathImg);
        Process process = processBuilder.start();
        System.out.println("test 3");

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        System.out.println("test 4");
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            result += line + "\n";
        }
        System.out.println("test 5");
        int exitCode = process.waitFor();
        System.out.println("Exit code: " + exitCode);
        System.out.println("end runNs");
        return result;
    }

    /***
     * Дефлотный метод выполняет запуск модели НС для распознавания на локальной машине - создает подпроцесс (как в консоли)
     * Пусти всегда одни и те же - дефолтные
     * @throws IOException
     * @throws InterruptedException
     */
    public static void defaultRunNs() throws IOException, InterruptedException {
        String pythonScript = "C:\\Users\\Public\\diplomaNS\\modelNS\\yolov7\\detect.py"; // Замените на путь к вашему файлу detect.py

        ProcessBuilder processBuilder = new ProcessBuilder("python", pythonScript,
                "--weights", "C:\\Users\\Public\\diplomaNS\\modelNS\\best5.pt",
                "--source", "C:\\Users\\Public\\diplomaNS\\img\\1.jpg",
                "--img", "1280");
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        int exitCode = process.waitFor();
        System.out.println("Exit code: " + exitCode);
    }

    /**
     * получение резуьтата после работы нейронной сети - парсинг результата
     * @param result большая страка с результатом
     */
    public void parseResultNs(String result) {
        if (result != null) {
            String[] partsResult = result.split("model is traced!");
            String[] resultsNs = partsResult[1].split("Done.");
            if (resultsNs[0].contains("\n"))
                resultsNs[0] = resultsNs[0].replace("\n", "");
            String[] codes = resultsNs[0].split("[^((\\d+_\\d+_\\d+)|(\\d+_\\d+))]");
            for (String s : codes) {
                if (s.contains("_")) {
                    this.result = s;
                    break;
                }
            }
        }
    }

}
