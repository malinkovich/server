package model;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.query.*;

import java.util.*;

public class OntologyService {

    protected String prefixMy = null;
    protected FileService fileService = new FileService();
    protected String requestPrefixMy = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                                    "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                                    "SELECT ?prefix\n" +
                                    "WHERE {\n" +
                                    "  ?prefix rdf:type owl:Ontology .\n" +
                                    "}";
    protected HashMap<String, String> requests = new HashMap<String, String>();
    {
        requests.put("class", "SELECT ?class\n" +
                                "WHERE { \n" +
                                "?class rdfs:subClassOf* owl:Thing .\n" +
                                "}");
        requests.put("property","SELECT ?property\n" +
                                "WHERE {\n" +
                                "  { ?property rdfs:subPropertyOf* owl:topObjectProperty . }\n" +
                                "  UNION\n" +
                                "  { ?property rdfs:subPropertyOf* owl:topDataProperty . }\n" +
                                "}");
        requests.put("value","SELECT ?value\n" +
                                "WHERE { \n" +
                                "?value rdfs:subPropertyOf* owl:topDataProperty .\n" +
                                "}");
        requests.put("iri","SELECT ?iri\n" +
                                "WHERE { \n" +
                                "?iri rdf:type owl:NamedIndividual .\n" +
                                "}");
    }

    /**
     * сформаировать запрос spqral
     * @param resource - поле, которое необходимо получить
     */
    public List<String> createRequestSPARQL(String resource) {
        String request = getPrefixes() + this.requests.get(resource);
        return doRequestSPARQL(request, resource, false, false);
    }

    /**
     * получение префикса онтологии (урла)
     * @return все префиксы для запроса
     */
    public String getPrefixes() {
        System.out.println("start getPrefixes");
        //получаю префикс my для дальнейших запросов
        if (prefixMy == null) {
            prefixMy = doRequestSPARQL(requestPrefixMy, "prefix", true, false).get(0);
        }
        String prefixes = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX my: <" + prefixMy + "#>\n";
        System.out.println("end getPrefixes");
        return prefixes;
    }

    /**
     * выполнение sparql запроса к онтологии
     * @param reqSparql sparql запрос
     * @param resources поле, которое нужно из него (запроса) получить
     * @param prefix если запрос для получения префикса - true, иначе false
     * @param literal если литерал - true, объектное свойство - false
     * @return возвращает названия объектов
     */
    public List<String> doRequestSPARQL(String reqSparql, String resources, boolean prefix, boolean literal) {
        System.out.println("start doRequestSPARQL");
        System.out.println("SPARQL request:\n" + reqSparql);
        List<String> resultRequest = new ArrayList<>();
        FileManager.get().addLocatorClassLoader(OntologyService.class.getClassLoader());

        Model model = FileManager.get().loadModel(fileService.getFilesFromFoder("rdf").getAbsolutePath());
        String queryStr = reqSparql;

        Query query = QueryFactory.create(queryStr);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        try {
            ResultSet result = qexec.execSelect();
            while (result.hasNext()) {
                QuerySolution soln = result.nextSolution();
                if (!literal) {
                    String[] resultPart = soln.getResource(resources).toString().split("#");
                    resultRequest.add(!prefix ? resultPart[1] : resultPart[0]); //.substring(0,resultPart[1].length()-1)
                } else {
                    String[] resultPart = soln.getLiteral(resources).toString().split("[\\^]");
                    resultRequest.add(resultPart[0]);
                }
            }
        }
        finally {
            qexec.close();
        }
        System.out.println("end doRequestSPARQL");
        return resultRequest;
    }

    public void addRequest(String resource, String property, String result) {
        System.out.println("start addRequest");
        if (result.contains("_"))
            result = result.replace("_", ".");
        String request = "SELECT ?" + resource + "\n" +
                "WHERE { \n" +
                "?" + resource + " my:" + property + " \"" + result + "\" .\n" +
                "}";
        this.requests.put(resource, request);
        System.out.println("end addRequest");
    }

    /**
     * метод для выполнения запроса ТОЛЬКО для получения информации об индивиде
     * @param individual - имя индивида
     * @return коллекция - из коллекции свойств и коллекции значений
     */
    public List<List<String>> getInformationByIndividual(String individual) {
        List<List<String>> result = new ArrayList<>();
        List<String> properties = new ArrayList<>();
        List<String> values = new ArrayList<>();

        List<String> resource = new ArrayList<>();
        resource.add("ObjectProperty"); //получаю объектные свойства и их значения
        resource.add("DatatypeProperty"); //получаю литеральные свойства и их значения
        for (String res : resource) {
            //получаю ОБЪЕКТНЫЕ свойства и их значения
            String selectProperties = "SELECT ?individualProperty ?individualValue\n" +
                    "WHERE {  \n" +
                    "my:" + individual + " ?individualProperty ?individualValue .\n" +
                    "?individualProperty rdf:type owl:" + res + " .\n" +
                    "}";
            String requestProperties = getPrefixes() + selectProperties;
            boolean literal = res.equals("DatatypeProperty");
            //свойства всегда получаем через getResource, поэтому литарел всегда false
            List<String> objectProperties = doRequestSPARQL(requestProperties, "individualProperty", false, false);
            List<String> objectValues = doRequestSPARQL(requestProperties, "individualValue", false, literal);
            properties.addAll(objectProperties);
            values.addAll(objectValues);
        }
        result.add(properties);
        result.add(values);
        return result;
    }
}
