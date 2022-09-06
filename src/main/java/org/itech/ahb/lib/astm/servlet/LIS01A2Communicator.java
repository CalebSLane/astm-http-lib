package org.itech.ahb.lib.astm.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.itech.ahb.lib.common.ASTMFrame;
import org.itech.ahb.lib.common.ASTMFrame.FrameType;
import org.itech.ahb.lib.common.ASTMInterpreter;
import org.itech.ahb.lib.common.ASTMMessage;

public class LIS01A2Communicator {

	public enum FrameError {
		WRONG_FRAME_NUMBER, MAX_SIZE_EXCEEDED, ILLEGAL_CHAR, BAD_CHECKSUM, ILLEGAL_START, ILLEGAL_END
	}

	private static final char CR = 0x0D;
	private static final char LF = 0x0A;
	private static final char SOH = 0x01;
	private static final char STX = 0x02;
	private static final char ETX = 0x03;
	private static final char EOT = 0x04;
	private static final char ENQ = 0x05;
	private static final char ACK = 0x06;
	private static final char DLE = 0x10;
	private static final char DC1 = 0x11;
	private static final char DC2 = 0x12;
	private static final char DC3 = 0x13;
	private static final char DC4 = 0x14;
	private static final char NAK = 0x15;
	private static final char SYN = 0x16;
	private static final char ETB = 0x17;

	private static final List<Character> RESTRICTED_CHARACTERS = Arrays.asList(SOH, STX, ETX, EOT, ENQ, ACK, DLE, NAK,
			SYN, ETB, LF, DC1, DC2, DC3, DC4);

	public static final int MAX_FRAME_SIZE = 64000;
	public static final int MAX_TEXT_SIZE = MAX_FRAME_SIZE - 7;

	private static final int ESTABLISHMENT_SEND_TIMEOUT = 150;// 15; // in seconds
	private static final int ESTABLISHMENT_RECEIVE_TIMEOUT = 200;// 20; // in seconds
	private static final int RECIEVE_FRAME_TIMEOUT = 300;// 30; // in seconds
	private static final int SEND_FRAME_TIMEOUT = 300;// 30; // in seconds

	private static final int MAX_SEND_RETRY_ATTEMPTS = 0;// 3;
	private static final int SEND_ATTEMPTS_WAIT = 10; // in seconds

	private static final int MAX_FRAME_RETRY_ATTEMPTS = 0;// 5;

	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final ASTMInterpreter interpreter;

	public LIS01A2Communicator(ASTMInterpreter interpreter) {
		this.interpreter = interpreter;
	}

	public List<ASTMMessage> receiveProtocol(BufferedReader reader, PrintWriter writer) {
		List<ASTMFrame> frames = new ArrayList<>();

		final Future<Boolean> establishedFuture = executor.submit(establishmentTaskReceive(reader, writer));
		Boolean established = false;
		try {
			established = establishedFuture.get(ESTABLISHMENT_RECEIVE_TIMEOUT, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			establishedFuture.cancel(true);
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!established) {
			executor.shutdown();
			return interpreter.interpretFramesToASTMMessages(frames);
		}

		try {
			boolean eotDetected = false;
			while (!eotDetected) {
				char startChar = (char) reader.read();
				if (startChar == EOT) {
					eotDetected = true;
				} else {
					final Future<Set<FrameError>> recievedFrameFuture = executor
							.submit(receiveNextFrameTask(reader, writer, frames));
					try {
						Set<FrameError> frameErrors = recievedFrameFuture.get(RECIEVE_FRAME_TIMEOUT, TimeUnit.SECONDS);

						if (startChar != STX) {
							frames.remove(frames.size() - 1);
							frameErrors.add(FrameError.ILLEGAL_START);
						}
						if (frameErrors.isEmpty()) {
							writer.append(ACK);
							writer.flush();
						} else {
							writer.append(NAK);
							writer.flush();
						}
					} catch (TimeoutException e) {
						recievedFrameFuture.cancel(true);
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		executor.shutdown();

		return interpreter.interpretFramesToASTMMessages(frames);
	}

	private Callable<Boolean> establishmentTaskReceive(BufferedReader reader, PrintWriter writer) {
		return new Callable<Boolean>() {
			@Override
			public Boolean call() throws IOException {
				if ((char) reader.read() == ENQ) {
					writer.append(ACK);
					writer.flush();
					return true;
				} else {
					writer.append(NAK);
					writer.flush();
					return false;
				}
			}
		};
	}

	private Callable<Set<FrameError>> receiveNextFrameTask(BufferedReader reader, PrintWriter writer,
			List<ASTMFrame> frames) throws IOException {
		return new Callable<Set<FrameError>>() {
			@Override
			public Set<FrameError> call() throws IOException {
				return readNextFrame(reader, writer, frames, (frames.size() + 1) % 8);
			}
		};
	}

	private Set<FrameError> readNextFrame(BufferedReader reader, PrintWriter writer, List<ASTMFrame> frames,
			int expectedFrameNumber) throws IOException {
		Set<FrameError> frameErrors = new HashSet<>();
		char frameNumberChar = (char) reader.read();
		if (expectedFrameNumber != Character.getNumericValue(frameNumberChar)) {
			frameErrors.add(FrameError.WRONG_FRAME_NUMBER);
		}
		char curChar = (char) reader.read();
		int frameSize = 0;
		StringBuilder textBuilder = new StringBuilder();
		while (curChar != ETB && curChar != ETX) {
			if (RESTRICTED_CHARACTERS.contains(curChar)) {
				frameErrors.add(FrameError.ILLEGAL_CHAR);
			}
			if (MAX_TEXT_SIZE < frameSize) {
				frameErrors.add(FrameError.MAX_SIZE_EXCEEDED);
			}
			textBuilder.append(curChar);
			++frameSize;
			curChar = (char) reader.read();
		}
		boolean finalFrame = (curChar == ETX);
		String text = textBuilder.toString();

		StringBuilder checksum = new StringBuilder();
		checksum.append((char) reader.read());
		checksum.append((char) reader.read());

		if (!checksumFits(checksum.toString(), frameNumberChar, text, curChar)) {
			frameErrors.add(FrameError.BAD_CHECKSUM);
		}
		if (CR != (char) reader.read()) {
			frameErrors.add(FrameError.ILLEGAL_END);
		}
		if (LF != (char) reader.read()) {
			frameErrors.add(FrameError.ILLEGAL_END);
		}
		if (frameErrors.isEmpty()) {
			ASTMFrame frame = new ASTMFrame();
			frame.setFrameNumber(Character.getNumericValue(frameNumberChar));
			frame.setType(finalFrame ? FrameType.END : FrameType.INTERMEDIATE);
			frame.setText(text);
			frames.add(frame);
		}
		return frameErrors;
	}

	public boolean sendProtocol(ASTMMessage message, BufferedReader reader, PrintWriter writer) {
		try {
			List<ASTMFrame> frames = interpreter.interpretASTMMessageToFrames(message);

			Boolean established = false;
			for (int i = 0; i <= MAX_SEND_RETRY_ATTEMPTS; i++) {
				final Future<Boolean> establishedFuture = executor.submit(establishmentTaskSend(reader, writer));
				try {
					established = establishedFuture.get(ESTABLISHMENT_SEND_TIMEOUT, TimeUnit.SECONDS);
				} catch (TimeoutException e) {
					establishedFuture.cancel(true);
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (established) {
					break;
				} else {
					try {
						Thread.sleep(SEND_ATTEMPTS_WAIT * 1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			if (!established) {
				executor.shutdown();
				termination(writer);
				return false;
			}

			for (int i = 0; i < frames.size(); i++) {
				int retries = 0;
				final Future<Boolean> sendFrameFuture = executor
						.submit(sendNextFrameTask(reader, writer, frames.get(i)));
				try {
					established = sendFrameFuture.get(SEND_FRAME_TIMEOUT, TimeUnit.SECONDS);
				} catch (TimeoutException e) {
					sendFrameFuture.cancel(true);
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				char response = (char) reader.read();
				if (response == ACK) {
					continue;
				} else if (response == EOT) {
					termination(writer);
					return false;
					// TODO
//					throw new Exception(
//							"remote server recieved the message successfully but asked that the connection be terminated");
				} else if (response == NAK) {
					--i;
					if (++retries > MAX_FRAME_RETRY_ATTEMPTS) {
						termination(writer);
						return false;
					}
					continue;
				} else {
					// TODO log
					--i;
					if (++retries > MAX_FRAME_RETRY_ATTEMPTS) {
						termination(writer);
						return false;
					}
					continue;
				}
			}
			termination(writer);
		} catch (RuntimeException e) {
			// TODO
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	private Callable<Boolean> establishmentTaskSend(BufferedReader reader, PrintWriter writer) {
		return new Callable<Boolean>() {
			@Override
			public Boolean call() throws IOException {
				writer.append(ENQ);
				writer.flush();
				char response = (char) reader.read();
				if (response == ACK) {
					return true;
				} else if (response == NAK) {
					return false;
				} else {
					return false;
				}
			}
		};
	}

	private Callable<Boolean> sendNextFrameTask(BufferedReader reader, PrintWriter writer, ASTMFrame frame) {
		return new Callable<Boolean>() {
			@Override
			public Boolean call() {
				StringBuilder frameBuilder = new StringBuilder();
				char frameNumber = Character.forDigit(frame.getFrameNumber(), 10);
				char frameTerminator = frame.getType() == FrameType.INTERMEDIATE ? ETB : ETX;
				frameBuilder.append(STX)//
						.append(frameNumber)//
						.append(frame.getText())//
						.append(frameTerminator)//
						.append(checksumCalc(frameNumber, frame.getText(), frameTerminator))//
						.append(CR)//
						.append(LF);
				writer.append(frameBuilder.toString());
				writer.flush();

				return true;
			}
		};
	}

	private void termination(PrintWriter writer) {
		writer.append(EOT);
		writer.flush();
	}

	private boolean checksumFits(String checksum, char frameNumber, String frame, char frameTerminator) {
		return checksum.equals(checksumCalc(frameNumber, frame, frameTerminator));
	}

	private String checksumCalc(char frameNumber, String frame, char frameTerminator) {
		int computedChecksum = 0;
		computedChecksum += (byte) frameNumber;
		for (byte curByte : frame.getBytes(Charset.forName(StandardCharsets.UTF_8.toString()))) {
			computedChecksum += curByte;
		}
		computedChecksum += (byte) frameTerminator;
		computedChecksum %= 256;
		return String.format("%02X", computedChecksum);
	}

}
