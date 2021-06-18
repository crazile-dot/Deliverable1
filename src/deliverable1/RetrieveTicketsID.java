package deliverable1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class RetrieveTicketsID {
	
	private static final Logger LOGGER = Logger.getLogger(RetrieveTicketsID.class.getName());
	
    private static String readAll(Reader rd) throws IOException {
	      StringBuilder sb = new StringBuilder();
	      int cp;
	      while ((cp = rd.read()) != -1) {
	         sb.append((char) cp);
	      }
	      return sb.toString();
	}

    public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
      InputStream is = new URL(url).openStream();
      try (
         BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
       ) {  
    	  String jsonText = readAll(rd);
          return new JSONArray(jsonText);
      } finally {
         is.close();
       }
    }

    
    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
      InputStream is = new URL(url).openStream();
      try (
         BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
    		  ){
         String jsonText = readAll(rd);
         return new JSONObject(jsonText);
       } finally {
         is.close();
       }
    }
   
    public static Date parseStringToDate(String string) throws ParseException{
	   
	   String format = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";	   
	   return new SimpleDateFormat(format).parse(string);
    }


    public static void main(String[] args) throws IOException, JSONException, ParseException {
		   
    	String projName ="RAMPART";
    	Integer j = 0;
    	Integer i = 0;
    	Integer total = 1;
		  //Get JSON API for closed bugs w/ AV in the project
		  do {
			 //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
			 j = i + 1000;
			 String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
			+ projName + "%22AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created&startAt="
			+ i.toString() + "&maxResults=" + j.toString();
			
			 JSONObject json = readJsonFromUrl(url);
			 //FileWriter myWriter = new FileWriter("C:\\Users\\crazile\\Desktop\\anotheroutput.txt");
			 //myWriter.write(json.toString());
			 JSONArray issues = json.getJSONArray("issues");
			 /*for (int k = 0; k < issues.length(); k++) {
				 System.out.println(issues.getJSONObject(k));
			 }*/
			 //System.out.println(issues.length());
			 //System.exit(0);
			 List<Date> ticketList = new ArrayList<>();
			 total = json.getInt("total");
			 for (; i < total && i < j; i++) {
			 	JSONObject field = issues.getJSONObject(i%1000);
			 	String fieldObject = field.getJSONObject("fields").get("resolutiondate").toString();
			 	ticketList.add(parseStringToDate(fieldObject));
			 }
			 ticketList.sort(null);
			 int ticketSize = ticketList.size();
			 System.out.println(ticketList);
			 
			 // prendo la lista dei ticket e creo una lista con tanti zeri quanti sono i mesi totali dal primo all'ultimo ticket
			 List<Integer> ret = new ArrayList<>();
			 Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"));
	         cal.setTime(ticketList.get(0));
	         int startYear = cal.get(Calendar.YEAR);
	         cal.setTime(ticketList.get(ticketSize-1));
	         int endYear = cal.get(Calendar.YEAR);
	         int nMonths = ((endYear+1)-startYear)*12;
	         
	         for(int l = 0; l < nMonths; l++) {
	        	 ret.add(0);
	         }
	         
	         //contiamo quanti ticket ci sono relativi ad ogni mese
	         for(int c1 = 0; c1 < ticketSize; c1++) {
	        	 int ticketCounter = 1;
	        	 cal.setTime(ticketList.get(c1));
	        	 int year1 = cal.get(Calendar.YEAR);
	             int month1 = cal.get(Calendar.MONTH);
	             for(int c2 = 1 + c1; c2 < ticketSize; c2++) {
	            	 cal.setTime(ticketList.get(c2));
	            	 int year2 = cal.get(Calendar.YEAR);
	                 int month2 = cal.get(Calendar.MONTH);
	            	 if(month1 == month2 && year1 == year2) {
	            		 ticketCounter = ticketCounter + 1;
	            	 }
	            	 else
	            		 break;
	             }
	             //riempiamo la lista con il numero di ticket per ciascun mese
	             int index = ((year1-startYear)*12)+month1;
	             ret.set(index, ticketCounter);
	         }
	         //sum è il numero totale di ticket nell'intero periodo osservato
	         int sum = 0;
	         for(Integer elem: ret) {
	        	  sum = sum + elem;
	         }
	         
	         for(Date date: ticketList) {
	        	 LOGGER.log(Level.INFO, String.valueOf(date));
	         }  

	         writeDataInCSV(ticketList);
	         
		  } while (i < total);
		  
	}
    
		
	public static void writeDataInCSV(List<Date> dataArray) throws IOException {
		Integer k = 0;
	    Integer l = 0;
	    Integer sumElemDataArrayFinal = 0;
	    Integer dataArraySize = dataArray.size();
	  
	    try (
	    	BufferedWriter br = new BufferedWriter(new FileWriter("C:\\Users\\crazile\\Desktop\\data.csv"));) {

		    // Get some useful information about the time frame
		    ArrayList<Integer> dataArrayFinal = new ArrayList<>();
		    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"));
		    cal.setTime(dataArray.get(0));
		    int startYear = cal.get(Calendar.YEAR);
		    cal.setTime(dataArray.get(dataArraySize - 1));
		    int endYear = cal.get(Calendar.YEAR);
		    int monthCounter = ((endYear + 1) - startYear) * 12;	         
		   
		    // Initialize array by 0-padding
		    for(; k < monthCounter; k++) {
		    	dataArrayFinal.add(0);
		    }
		   
		    // Write header of the csv file produced in output
		    StringBuilder sb = new StringBuilder();
		    //sb.append(type + " " + resolution);
		    sb.append(",");
		    sb.append("Resolution date");
		    sb.append("\n");
		    br.write(sb.toString());
		   
		    // Cycle on 'dataArray' counting for each month how many 
		    // 'types' have been 'resolution'
		    for(; l < dataArraySize; l++) {
		    	int valueCounter = 1;
		    	cal.setTime(dataArray.get(l));
		    	int year = cal.get(Calendar.YEAR);
		    	int month = cal.get(Calendar.MONTH);
		       
		        for(int m = l + 1; m < dataArraySize; m++) {
		        	cal.setTime(dataArray.get(m));
		        	int year2 = cal.get(Calendar.YEAR);
		            int month2 = cal.get(Calendar.MONTH);
		      	 
		            if(month == month2 && year == year2) {
		            	valueCounter = valueCounter + 1;
		            	l = l + 1;
		            } else {
		            	break;
		            }
		        }
		       
		        // Update the respective 'valueCounter' found in 'dataArrayFinal'
		        int index = ((year - startYear) * 12) + month;
		        dataArrayFinal.set(index, valueCounter);
		    }		
		   
		   
		    // Cycle on 'dataArrayFinal' to write 'valueCounter' associated
		    // to month and year to the csv file
		    int indexYear = 0;
		    int indexMonth = 1;
		    for(int elemDataArrayFinal : dataArrayFinal) {
		    	sumElemDataArrayFinal = sumElemDataArrayFinal + elemDataArrayFinal;
		  	 
		    	if (indexMonth == 13) {
		    		indexMonth = 1;
			        indexYear ++;
		    	}
		    	int year = startYear + indexYear;
		    	String dateForDataSet = indexMonth + "/" + year;
		     
		    	// Write data in csv file produced in output
		    	StringBuilder sb2 = new StringBuilder();
		    	sb2.append(elemDataArrayFinal);
		    	sb2.append(",");
		    	sb2.append(dateForDataSet);
		    	sb2.append("\n");
		    	br.write(sb2.toString());
				         
		    	indexMonth ++;
		   }
	    }
	}
	 
}
