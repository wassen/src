//import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Random;
import java.lang.StringBuffer;

import enumerate.Action;
import gameInterface.AIInterface;
import structs.CharacterData;
import structs.FrameData;
import structs.GameData;
import structs.Key;
import structs.MotionData;
import commandcenter.CommandCenter;

/**
 * @author Phakhawat Sarakit Fight Game AI Competition 2014 Category 3 T3c
 * 
 *         Create at 19/8/2014 if you have question feel free ask me i really
 *         appreciate. contact me at email: toonja1990@hotmail.com facebook
 *         https://www.facebook.com/toonja20th
 */
public class research implements AIInterface {
	// my note skills.
	// STAND_D_DB_BB run head butt long range 250
	// STAND_D_DB_BA long punch good range 170
	// STAND_F_D_DFA tuub close range 130
	// STAND_F_D_DFB slide
	// STAND_D_DF_FA direct fireball
	// STAND_D_DF_FB up fireball
	// AIR_UB jump kick
	// AIR_F_D_DFB long jump kick
	// AIR_DB jump stum

	GameData gd;
	Key inputKey;
	boolean playerNumber;
	FrameData frameData;
	CommandCenter cc;
	int X = 0;
	String myAction = null;
	int attack_type = 0;
	CharacterData characterdata;
	CharacterData characterdata2;
	int skillcounter = 0;
	int punchcounter = 0;

	// queue部の変数
	// FrameData fdArray[];
	Deque<FrameData> deque;
//	boolean initDequeFlag;
	int delay;

	long realRemainingTime;
	boolean newRound;
	float realp1;
	float realp2;
	int round;

	Random rnd;

	@Override
	public synchronized int initialize(GameData gameData, boolean playerNumber) {
		gd = gameData;
		this.playerNumber = playerNumber;
		this.inputKey = new Key();
		cc = new CommandCenter();
		frameData = new FrameData();
		rnd = new Random();

		newRound = true;
		// delayの設定
		try {
			FileReader fileReader;
			String path = "delay.txt";
			File file = new File(path);

			if (!file.exists())
				file.createNewFile();
			fileReader = new FileReader(path);
			int i;
			String sDelay = new String();
			while ((i = fileReader.read()) != -1) {
				StringBuffer buffer = new StringBuffer();
				buffer.append(sDelay);
				buffer.append(String.valueOf((char) i));
				sDelay = buffer.toString();
			}
			sDelay = sDelay.replaceAll("\r\n", "");
			sDelay = sDelay.replaceAll(" ", "");
			delay = Integer.parseInt(sDelay);
			System.out.println("delay = " + delay);
			fileReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Dequeの初期化
		deque = new LinkedList<FrameData>();
//		initDequeFlag = false;
		round = 1;
		return 0;

	}

	@Override
	public synchronized void getInformation(FrameData frameData) {
		
		if (!frameData.emptyFlag && frameData.getRemainingTime() > 0) {
			realRemainingTime = frameData.getRemainingTime();
			realp1 = frameData.P1.getHp();
			realp2 = frameData.P2.getHp();
		}

		FrameData fd = new FrameData(frameData);
//		if (!initDequeFlag && frameData.remainingTime != -1) {
//			for (int i = 0; i < delay; i++) {
//				deque.offerFirst(fd);
//			}
//			initDequeFlag = true;
//		}

		deque.offerFirst(fd);
		this.frameData = deque.pollLast();

		cc.setFrameData(this.frameData, playerNumber);

	}

	@Override
	public synchronized void processing() {

		if (!frameData.emptyFlag && frameData.getRemainingTime() > 0) {

			characterdata2 = cc.getEnemyCharacter();
			characterdata = cc.getMyCharacter();

			int posx = characterdata.getX();
			int posx2 = characterdata2.getX();

			// get X Distance
			X = cc.getDistanceX();
			// System.out.println("X=" + X);
			// get my Action
			myAction = cc.getMyCharacter().getAction().name();
			// ////////////
			// get opponet action in this case i get attack type
			Action oppAct = cc.getEnemyCharacter().getAction();
			MotionData oppMotion = new MotionData();
			if (playerNumber)
				oppMotion = gd.getPlayerTwoMotion().elementAt(oppAct.ordinal());

			else
				oppMotion = gd.getPlayerOneMotion().elementAt(oppAct.ordinal());
			attack_type = oppMotion.getAttackType();

			// ////////////
			if (cc.getskillFlag()) {

				inputKey = cc.getSkillKey();

			} else {

				inputKey.empty();
				cc.skillCancel();

				// spacial skill
				if (cc.getMyEnergy() >= 400) {
					cc.commandCall("STAND_D_DF_FC");
					// reset limit skill to 0
					skillcounter = 0;

				}

				// attack with skill that use energy //slide attack
				else if (X < 220
						&& X > 150
						&& cc.getMyEnergy() < 400
						&& cc.getMyEnergy() > 50
						&& skillcounter != 15
						&& !cc.getEnemyCharacter().getState().name()
								.equals("AIR")) {
					cc.commandCall("STAND_F_D_DFB");
					// limit skill use

					skillcounter++;

				}
				// attack with nomal skill no energy use
				else if (X > 63 && X < 150 && cc.getMyEnergy() < 400) {
					cc.commandCall("STAND_FB");

				}

				// /////////////////////////////
				// close combat
				// /////////////////////////////
				// punch in very close range
				else if (X < 32) {
					if (punchcounter != 5) {
						cc.commandCall("STAND_B");

						punchcounter++;

					}// combo with punch
					else {

						cc.commandCall("CROUCH_FB");
						punchcounter = 0;

					}
				}
				// throw
				else if (X > 32
						&& X < 63
						&& !cc.getEnemyCharacter().getAction().name()
								.startsWith("AIR")) {
					cc.commandCall("THROW_B");

				}
				// combo with trow
				else if (cc.getEnemyCharacter().getAction().name()
						.equals("THROW_SUFFER")
						&& cc.getMyEnergy() >= 30) {
					cc.commandCall("STAND_D_DF_FB ");

				}

				// end of close combat
				// /////////////////////////////

				// counter attack
				// counter spacial attack
				else if (cc.getEnemyCharacter().getAction().name()
						.equals("STAND_D_DF_FC")) {
					cc.commandCall("FOR_JUMP");

				}

				else if (myAction.equals("FOR_JUMP")) {
					cc.commandCall("AIR_DB");

				}

				// ///////////////////////////////
				// defend tactics
				// attack type
				// 1=high
				// 2=middle
				// 3=low
				// 4=throw
				// ///////////////////////////////
				// high attack type
				else if (attack_type == 1
						&& X < 200
						&& !cc.getEnemyCharacter().getAction().name()
								.equals("STAND_D_DF_FC")) {
					cc.commandCall("CROUCH_FB");

				}
				// middle attack type
				else if (attack_type == 2 && X < 200) {
					cc.commandCall("STAND_FA");

				}
				// low attack type
				else if (attack_type == 3
						&& X < 200
						&& !cc.getEnemyCharacter().getAction().name()
								.equals("STAND_D_DF_FC")) {
					cc.commandCall("CROUCH_FB");

				}
				// enermy use air skill
				else if (cc.getEnemyCharacter().getAction().name()
						.startsWith("AIR")
						&& X < 230) {
					cc.commandCall("AIR_UB");
				}

				// jump out when at the end of corner
				else if (posx == 710 || posx == -170 && cc.getMyEnergy() < 400) {
					cc.commandCall("FOR_JUMP");

				}
				// end of defend tactic
				// //////////////////////////////////

				// automatic move
				else if (posx > posx2) {
					inputKey.L = true;
				} else {
					inputKey.R = true;
				}
				if (realRemainingTime > 59000) {
					newRound = true;
				}
				if (realRemainingTime < 170 && newRound) {
					
					
					try {
//						exportRoundNumber();
						exportScore();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

//	public void exportRoundNumber()throws IOException{
//		
////		String path = "data\\aiData\\research\\setting.ini";
//		String path = "data\\aiData\\research\\delay.txt";
//		StringBuffer buffer = new StringBuffer();
//		BufferedReader bufferReader = new BufferedReader(new FileReader(path));
//		String line;
//		
////		File file = new File(path);
////		if (!file.exists())
////			file.createNewFile();これやるんならディレクトリも作りたいね
//
//		while ((line = bufferReader.readLine()) != null) {
//			String status = line.substring(line.indexOf("<"),
//					line.indexOf(">") + 1);
//
//			if (status.equals("[round]")) {
//				buffer.append(status + round + "\r\n");
//				// fileWriter.write(buffer.toString());
//			} else {
//				// fileWriter.write(line);
//				buffer.append(line + "\r\n");
//			}
//
//		}
//
//		FileWriter fileWriter = new FileWriter(path, false);
//		fileWriter.write(buffer.toString());
//
//		bufferReader.close();
//		fileWriter.close();
//	}
	
	public void exportScore() throws IOException {
		realp1 = frameData.P1.getHp();
		realp2 = frameData.P2.getHp();
		System.out.println("P1の" + "スコアは"
				+ Math.round(realp2 / (realp2 + realp1) * 1000));
		System.out.println("P2の" + "スコアは"
				+ Math.round(realp1 / (realp2 + realp1) * 1000));

		String path = "data\\aiData\\research\\score.csv";

		File file = new File(path);

		if (!file.exists())
			file.createNewFile();

		FileWriter filewriter;

		filewriter = new FileWriter(file, true);

		filewriter.write("," + Math.round(realp2 / (realp2 + realp1) * 1000));

		filewriter.close();

		newRound = false;

	}

	@Override
	public synchronized Key input() {
		// TODO Auto-generated method stub
		return inputKey;
	}

	@Override
	public synchronized void close() {

		// TODO Auto-generated method stub

	}

	@Override
	public String getCharacter() {
		// TODO Auto-generated method stub
		return CHARACTER_LUD;
	}

}
