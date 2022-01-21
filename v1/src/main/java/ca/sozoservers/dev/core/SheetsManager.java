package ca.sozoservers.dev.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendCellsRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.UpdateCellsRequest;

public class SheetsManager {
    
    private static Sheets sheetsService;
    private static String APPLICATION_NAME = "Plutus";
    private static String SPREADSHEET_ID = "1HcRx5O3-uSvY8sUZ8f3GavHxVcBKTmo0sbLYH4_pmsk";

    public static enum SheetID{
        Sponsors(0);

        private int id;
        private SheetID(int id){
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    static{
        try {
            sheetsService = getSheetsService();
        } catch (IOException | GeneralSecurityException e) {
            ErrorManager.throwError(Thread.currentThread(), e);
        }
    }

    private static Credential authorize() throws IOException, GeneralSecurityException {
        InputStream in = FileManager.getResourceAsStream("keys/key.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(), new InputStreamReader(in));

        List<String> scopes = Arrays.asList(SheetsScopes.SPREADSHEETS);

        GoogleAuthorizationCodeFlow flow = 
        new GoogleAuthorizationCodeFlow.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), clientSecrets, scopes)
        .setDataStoreFactory(new FileDataStoreFactory(new File("tokens")))
        .setAccessType("offline")
        .build();

        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");

        return credential;
    }

    private static Sheets getSheetsService() throws IOException, GeneralSecurityException {
        Credential credential = authorize();
        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), credential)
        .setApplicationName(APPLICATION_NAME)
        .build();
    }

    public static Sheet readSheet(String sheetName, List<String> ranges, boolean gridData, SheetID sheetId) {
            ranges.forEach(range -> ranges.set(ranges.indexOf(range), sheetName+"!"+range));
        try {
            Spreadsheet spreadsheet = sheetsService.spreadsheets().get(SPREADSHEET_ID).setRanges(ranges).setIncludeGridData(gridData).execute();
            Sheet sheet = spreadsheet.getSheets().get(0);
            return sheet;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeSheet(List<RowData> rowData, SheetID sheetId){
        try {
            AppendCellsRequest appendResponse = new AppendCellsRequest();
            appendResponse.setSheetId(sheetId.getId());
            appendResponse.setRows(rowData);
            appendResponse.setFields("userEnteredValue,userEnteredFormat.backgroundColor");

            List<Request> requests = new ArrayList<Request>();
            requests = new ArrayList<Request>();
            requests.add(new Request().setAppendCells(appendResponse));

            BatchUpdateSpreadsheetRequest batchRequests = new BatchUpdateSpreadsheetRequest();
            batchRequests = new BatchUpdateSpreadsheetRequest();
            batchRequests.setRequests(requests);

            sheetsService.spreadsheets().batchUpdate(SPREADSHEET_ID, batchRequests).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteRowSheet(int row, SheetID sheetId){
        BatchUpdateSpreadsheetRequest content = new BatchUpdateSpreadsheetRequest();
        Request request = new Request()
        .setDeleteDimension(new DeleteDimensionRequest()
            .setRange(new DimensionRange()
            .setSheetId(sheetId.getId())
            .setDimension("ROWS")
            .setStartIndex(row)
            .setEndIndex(row+1)
            )
        );
    
        List<Request> requests = new ArrayList<Request>();
        requests.add(request);
        content.setRequests(requests);
        try {
            sheetsService.spreadsheets().batchUpdate(SPREADSHEET_ID, content).execute();
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void updateRowSheet(List<RowData> rowData, int row, SheetID sheetId){
        BatchUpdateSpreadsheetRequest content = new BatchUpdateSpreadsheetRequest();
        Request request = new Request()
        .setUpdateCells(new UpdateCellsRequest()
            .setRange(new GridRange()
                .setSheetId(sheetId.getId())
                .setStartRowIndex(row)
                .setEndRowIndex(row+1)
            ).setRows(rowData)
            .setFields("userEnteredValue,userEnteredFormat.backgroundColor")
        );
    
        List<Request> requests = new ArrayList<Request>();
        requests.add(request);
        content.setRequests(requests);    
        try {
            sheetsService.spreadsheets().batchUpdate(SPREADSHEET_ID, content).execute();
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
