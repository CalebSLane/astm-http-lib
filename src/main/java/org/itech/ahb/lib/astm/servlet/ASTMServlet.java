package org.itech.ahb.lib.astm.servlet;

import java.net.ServerSocket;
import java.net.Socket;

import org.itech.ahb.lib.common.ASTMInterpreterFactory;

public class ASTMServlet {

	private final ASTMHandlerMarshaller astmMessageMarshaller;
	private final ASTMInterpreterFactory astmInterpreterFactory;
	private final int listenPort;

	public ASTMServlet(ASTMHandlerMarshaller astmMessageMarshaller, ASTMInterpreterFactory astmInterpreterFactory,
			int listenPort) {
		this.astmMessageMarshaller = astmMessageMarshaller;
		this.astmInterpreterFactory = astmInterpreterFactory;
		this.listenPort = listenPort;
	}

	public void listen() {
		try (ServerSocket serverSocket = new ServerSocket(listenPort)) {
			System.out.println("Server is listening on port " + listenPort);
			// Communication Endpoint for the client and server.
			while (true) {
				// Waiting for socket connection
				Socket s = serverSocket.accept();
				new ASTMReceiveThread(s, astmInterpreterFactory, astmMessageMarshaller).start();

			}
		} catch (Exception e) {

		}
	}

//	Socket clientSocket = null;
//	public static char STX = '\002';
//	public static char ETX = '\003';
//	public static char ETB = '\027';
//	public static char EOT = '\004';
//	public static char ENQ = '\005';
//	public static char ACK = '\006';
//	public static char NAK = '\025';
//	public static char CR = '\r';
//	public static char LF = '\n';
//	public static char MOR = '>';
//	public static char FS = '\034';
//	public static char GS = '\035';
//	public static char RS = '\036';
//	public static char SFS = '\027';
//	public static char VT = 0x0B; // END OF BLOCK 011
//	public static Vector<String> vecMessages = new Vector<>();
//	private static int currentMsgCount = 0;
//	private static ServerSocket server;
//	private static Socket connection;
//	private static Message mes = new Message();
//
//	public static void main(String args[]) throws IOException, InterruptedException {
//		server = new ServerSocket(12221);
//		boolean stopped = false;
//
//		System.out.println(" start... ");
//		connection = server.accept();
//		System.out.println("wait for connection");
//		BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//		DataOutputStream outToClient = new DataOutputStream(connection.getOutputStream());
//		String currentMsg = "";
//		int clientIntMessage;
//
////	    String h1, s2, s3, s4, s5, s6 = "";
////	    h1 = "1H|@^\\|ODM-IdfDGIWA-36|||GeneXpert     PC^GeneXpert^4.8|||||LIS||P|1394-97|20070521100245" + ProtocolASCII.LF
////	            + "P|1" + ProtocolASCII.LF
////	            + "O|1|SID-818||^^^TestId-12|S|20070812140500|||||A||||ORH||||||||||Q" + ProtocolASCII.LF
////	            + "L|1|F" + ProtocolASCII.LF;
////	    s2 = "P|1";
////	    s3 = "O|1|SID-818||^^^TestId-12|S|20070812140500|||||A||||ORH||||||||||Q";
////	    //s4 = "O|2|SID-818||^^^TestId-14|S|20070812140600|||||A||||ORH||||||||||Q";
////	    //s5 = "O|3|SID-818||^^^TestId-16|S|20070812140700|||||A||||ORH||||||||||Q";
////	    s6 = "L|1|F";
//		//
////	    String retmsg = h1;
////	    //logException("OrderMessae   :" + retmsg);
////	    retmsg = ProtocolASCII.STX + retmsg + ProtocolASCII.CR + ProtocolASCII.ETX + ProtocolMessage.getCheckSum(retmsg) + ProtocolASCII.CR + ProtocolASCII.LF;
//
//		clientIntMessage = inFromClient.read();
//		// while (clientIntMessage != ProtocolASCII.EOT) {
//		while (true) {
//
//			while (clientIntMessage != EOT) {
//				clientIntMessage = inFromClient.read();
//				currentMsg += String.valueOf(Character.toChars(clientIntMessage));
//				// System.out.println(currentMsg);
//				if (clientIntMessage == ENQ) {
//					outToClient.writeBytes("" + ACK);
//					System.out.println("[ACK] on Analyzer [ENQ]");
//				} else if (clientIntMessage == ACK) {
//
//					System.out.println("Analyzer [ACK]");
//					if (vecMessages.size() == currentMsgCount) {
//						vecMessages.clear();
//						currentMsgCount = 0;
//						outToClient.writeBytes("" + EOT);
//						System.out.println("Host [EOT]");
//					} else {
//						String msg = vecMessages.get(currentMsgCount++);
//
//						outToClient.writeBytes(msg);
////	                                System.out.println("Msg " + msg.substring(0, msg.length() - 4));
//					}
//				} else if (clientIntMessage == LF) {
//					outToClient.writeBytes("" + ACK);
//				} else if (clientIntMessage == NAK) {
//					System.out.println(" Analyzer sent [NAK] ");
//				}
//
//			}
//
//			System.out.println(currentMsg);
//			mes.parser(currentMsg);
//
//			clientIntMessage = 0;
//			currentMsg = "";
//
//		}
////	        connection.close();
////	        stopped = true;
//	}
//
//	public static class Message {
////	        machine Send This Query   ==>6.3.2.1.5 Example of Upload Message – Instrument System Sends Host Query
////	        H|@^\|b4a88d9adab947a7b3dca2b534119c25||ICU^GeneXpert^1.0|||||LIS||P|1394-97|20070521100245
////	        Q|1|PatientID-556^SpecimenID-888||||||||||O@N
////	        L|1|N
//
//	        public static Vector<String> vecMessages = new Vector<>();
////	        make message for machine
//
//	        public String HeaderMessage() {
//	            String retmsg = "H|@^\\|ccc6ade20d3623314sffa3e287f2314ad||LIS|||||ICU^GeneXpert^1.0||P|1394-97|20070521100245";
////	        System.out.println("HeaderMessage  :" + retmsg);
//	            retmsg = STX + retmsg + CR + ETX + getCheckSum(retmsg) + CR + LF;
//
//	            return retmsg;
//	        }
//	        //  P|1
//	        //      6.3.1.3.3 Patient Information Record     Find in Document and make it as per document
//
//	        public String PatientMessage(Patient pat) {  //(Patient pat)
//
//	            String retmsg = "P|1|||" + pat.getMRNO() + "|^" + pat.getPatientName() + "||" + pat.getDOB() + "|" + pat.getGender() + "|||||^Dr.SKM-LAS||||||||||||^^^EAST";
//
//	            retmsg = STX + retmsg + CR + ETX + getCheckSum(retmsg) + CR + LF;
//
//	            return retmsg;
//	        }
////	                6.3.1.3.4 Test Order Record      Find in Document
//
//	        public String orderMessage(String sampleId, String testIds, String orderType, String rackId, String positionNumber, String priority) {
//	            DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmSS");
//	            String sysDate = dateFormat.format(new Date());
//
//	            String retmsg = "3O|1|" + rackId + "^" + positionNumber + "^" + sampleId + "^B||" + testIds + "|" + priority + "|" + sysDate + "|||||" + orderType + "";
//
//	            retmsg = STX + retmsg + CR + ETX + getCheckSum(retmsg) + CR + LF;
//
//	            return retmsg;
//	        }
//
////	        6.3.1.3.5 Message Terminator Record    Find in Document
//	        public String terminatorMessage(String type) {
//	            String retmsg = "L|1|" + type;
//
//	            retmsg = STX + retmsg + CR + ETX + getCheckSum(retmsg) + CR + LF;
//	            return retmsg;
//	        }
//
//	        public String getCheckSum(String msg) {
//	            int sum = 0;
//	            for (int i = 0; i < msg.length(); i++) {
//	                sum += msg.charAt(i);
//	            }
//	            sum += 16; //adding CR and ETX AND ETB
//	            sum = sum % 256;
//	            String checksum = Integer.toHexString(sum).toUpperCase();
//	            if (checksum.length() == 1) {
//	                checksum = "0" + checksum;
//	            }
//	            //System.out.println("\n Check Sum is ="+checksum);
//	            return checksum;
//	        }
//
//	        public void parser(String input) {
////	        Use StringTokenizer for split or split
//
//	            if (input.charAt(1) == 'Q' || input.charAt(2) == 'Q') {
//
//	                //Q|1|PatientID-556^SpecimenID-888||||||||||O@N
////	                        Split it and get information which machine send in Query SampleId and other
//	                String rackId = "get from Query to check document";
//	                String positionNumber = "get from Query to check document";
//	                String sampleId = "get from Query to check document";
//
////	                        this.FetchOrders1(machineId, sampleId);     // for dummy Sample Run
//	                this.FetchOrders1("abc", sampleId);
//	                this.setMesType("Q");
//
//	            }
//
//	        }
//
//	        public void FetchOrders1(String machineId, String sampleId) {
//	            try {
//	                this.vecMessages.add(HeaderMessage());
//	                this.vecMessages.add(PatientMessage()); //Define patient information
//	                this.vecMessages.add(orderMessage(sampleId, "test", "N", "rackId", "positionNumber", "R"));
//	                this.vecMessages.add(terminatorMessage("N"));
//
//	            } catch (Exception e) {
//	                e.printStackTrace();
//	            }
//	        }
//
//	    }`
//
//	@Setter
//	private MessageHandler messageHandler;
//
//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	protected void doGet(HttpServletRequest theReq, HttpServletResponse theResp) throws ServletException, IOException {
//		String message = "GET is not supported";
//		writeResponse(theResp, HttpServletResponse.SC_BAD_REQUEST, message);
//	}
//
//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	protected void doPost(HttpServletRequest theReq, HttpServletResponse theResp) throws ServletException, IOException {
//
//		ASTMRequestDecoder decoder = new ASTMRequestDecoder(theReq);
//		try {
//			decoder.decode();
//		} catch (DecodeException e) {
//			log.error("Request failure for " + theReq.getRequestURI(), e);
//			writeResponse(theResp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
//			return;
//		} catch (ChecksumValidationException e) {
//			log.error("checksum verification filure for ", theReq.getRequestURI(), e);
//			writeResponse(theResp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
//			return;
//		}
//
//
//		ASTMResponse response;
//		try {
//			response = messageHandler.handleMessage(decoder.getFrame());
//		} catch (MessageProcessingException e) {
//			log.error("Processing problem for " + theReq.getRequestURI(), e);
//			writeResponse(theResp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
//			return;
//		}
//
//		theResp.setCharacterEncoding(StandardCharsets.UTF_8.toString());
//		theResp.setStatus(response.getResponseCode());
//
//		theResp.getWriter().append(response.getResponseMessage());
//		theResp.getWriter().flush();
//
//	}
//
//	private void writeResponse(HttpServletResponse theResp, int status, String message) {
//		theResp.setStatus(status);
//		OutputStream outStream = theResp.getOutputStream();
//		outStream.write(message.getBytes(StandardCharsets.UTF_8));
//		outStream.flush();
//	}

}
