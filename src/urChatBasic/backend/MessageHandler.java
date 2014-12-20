package urChatBasic.backend;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;

import urChatBasic.base.Constants;
import urChatBasic.base.IRCServerBase;
import urChatBasic.base.MessageHandlerBase;
import urChatBasic.base.UserGUIBase;

/** This class will Handle the message it has received and assign an approriate 
 * class that will parse the string and then
 * @author Matt
 *
 */
public class MessageHandler {
	static Set<IDGroup> groupIDs = new HashSet<IDGroup>();
	static Set<IDSingle> singleIDs = new HashSet<IDSingle>();
	
	IRCServerBase myServer;
	UserGUIBase gui;
	private static final char CHANNEL_DELIMITER = '#';
	private static final char CTCP_DELIMITER = '\001';
	private static final char SPACES_AHEAD_DELIMITER = ':';
	private static final int MESSAGE_LIMIT = 510;
	private static final String END_MESSAGE = "\r\n";
	
	/**
	 * Assign the correct 
	 * @param server
	 * @param messageID
	 */
	public MessageHandler(String receivedText){
		//this.myServer = server;
		
		if(groupIDs.isEmpty())
			addRanges();
		if(singleIDs.isEmpty())
			addSingles();
		
		Message receivedMessage = new Message(receivedText);
		
		boolean handled = false;
		
		
		if(!handled)
		for(IDSingle testSingle : singleIDs)
			if(testSingle.type.equals(MessageIdType.NUMBER_ID) && testSingle.isEqual(receivedMessage.idCommandNumber)){
				testSingle.handlerType.messageExec(receivedMessage);
				handled = true;
				break;
			}

		if(!handled)
		for(IDSingle testSingle : singleIDs)
			if(testSingle.type.equals(MessageIdType.STRING_ID) && testSingle.isEqual(receivedMessage.idCommand)){
				testSingle.handlerType.messageExec(receivedMessage);
				handled = true;
				break;
			}
		
		if(!handled)
		for(IDGroup testRange : groupIDs)
			if(testRange.type.equals(MessageIdType.NUMBER_ID) && testRange.inRange(receivedMessage.idCommandNumber)){
				testRange.handlerType.messageExec(receivedMessage);
				handled = true;
				break;
			}
			
		if(!handled)
		handleDefault(receivedMessage.toString());
		
	}
	
	public static void main(String[] args){
		runTests();
	}
	
	
	private static void runTests(){
		new MessageHandler(":kornbluth.freenode.net NOTICE * :*** Looking up your hostname...");
		new MessageHandler(":kornbluth.freenode.net NOTICE * :*** Checking Ident");
		new MessageHandler(":kornbluth.freenode.net NOTICE * :*** Couldn't look up your hostname");
		new MessageHandler(":kornbluth.freenode.net NOTICE * :*** No Ident response");
		new MessageHandler(":kornbluth.freenode.net 001 matty_r :Welcome to the freenode Internet Relay Chat Network matty_r");
		new MessageHandler(":kornbluth.freenode.net 002 matty_r :Your host is kornbluth.freenode.net[82.96.64.4/6667], running version ircd-seven-1.1.3");
		new MessageHandler(":kornbluth.freenode.net 003 matty_r :This server was created Mon Dec 31 2012 at 22:38:44 CET");
		new MessageHandler(":kornbluth.freenode.net 005 matty_r CHANTYPES=# EXCEPTS INVEX CHANMODES=eIbq,k,flj,CFLMPQScgimnprstz CHANLIMIT=#:120 PREFIX=(ov)@+ MAXLIST=bqeI:100 MODES=4 NETWORK=freenode KNOCK STATUSMSG=@+ CALLERID=g :are supported by this server");
		new MessageHandler(":kornbluth.freenode.net 005 matty_r CASEMAPPING=rfc1459 CHARSET=ascii NICKLEN=16 CHANNELLEN=50 TOPICLEN=390 ETRACE CPRIVMSG CNOTICE DEAF=D MONITOR=100 FNC TARGMAX=NAMES:1,LIST:1,KICK:1,WHOIS:1,PRIVMSG:4,NOTICE:4,ACCEPT:,MONITOR: :are supported by this server");
		new MessageHandler(":kornbluth.freenode.net 005 matty_r EXTBAN=$,ajrxz WHOX CLIENTVER=3.0 SAFELIST ELIST=CTU :are supported by this server");
		new MessageHandler(":kornbluth.freenode.net 251 matty_r :There are 179 users and 86163 invisible on 25 servers");
		new MessageHandler(":kornbluth.freenode.net 252 matty_r 26 :IRC Operators online");
		new MessageHandler(":kornbluth.freenode.net 253 matty_r 7 :unknown connection(s)");
		new MessageHandler(":kornbluth.freenode.net 254 matty_r 49396 :channels formed");
		new MessageHandler(":kornbluth.freenode.net 255 matty_r :I have 9031 clients and 2 servers");
		new MessageHandler(":kornbluth.freenode.net 265 matty_r 9031 15000 :Current local users 9031, max 15000");
		new MessageHandler(":kornbluth.freenode.net 266 matty_r 86342 104047 :Current global users 86342, max 104047");
		new MessageHandler(":kornbluth.freenode.net 250 matty_r :Highest connection count: 15001 (15000 clients) (2686643 connections received)");
		new MessageHandler(":kornbluth.freenode.net 375 matty_r :- kornbluth.freenode.net Message of the Day - ");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- Welcome to kornbluth.freenode.net in Frankfurt, DE, EU. ");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- Thanks to Probe Networks (www.probe-networks.de) for");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- sponsoring this server!");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :-  ");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- KORNBLUTH, CYRIL M. [1923-1958].  Born in New York City and");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- an active member of the Futurians, Cyril Kornbluth sold his");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- first story professionally at age 15.  By the 1940's his");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- stories appeared widely under several pennames, including");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- S.D. Gottesman and Cecil Corman.  He left his university");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- studies in Chicago to serve in the Army in Europe during");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- WWII, then returned to school and work in Chicago until he");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- began writing full-time in 1951.  The author of The Marching");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- Morons and the novels The Space Merchants (with Frederik");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- Pohl) and The Syndic, he's best known for his biting social");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- satire and his collaborations with Pohl and with Judith");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- Merril.");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- Welcome to freenode - supporting the free and open source");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- software communities since 1998.");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :-  ");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- **************************************************************");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :-                       SECURITY ALERT");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :-  ");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- Over the weekend of 13th-14th September freenode staff noticed");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- some compromised binaries present on a number of servers. ");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- The servers in question have been removed from the network and");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- shut down.  However, it's possible that network traffic  -");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- including SSL traffic - has been sniffed and passwords");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- exposed.");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :-  ");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- We therefore recommend that all users change their nickserv ");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- password(s) to a new value which is not shared with any");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- other service.");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :-  ");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- You can do this with /msg nickserv set password newpasshere");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :-  ");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- Please note that investigation is ongoing to discover the root");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- cause of the attack, and until this investigation is complete");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- we cannot be 100% certain that all traces of the compromises ");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- have been removed. We may have to ask you to change your  ");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- passwords again after analysis has completed.");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :-  ");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- Further details will appear on https://blog.freenode.net/");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :-  ");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- **************************************************************");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :-  ");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- By connecting to freenode you indicate that you have read and");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- accept our policies as set out on http://www.freenode.net");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- freenode runs an open proxy scanner. Please join #freenode for");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- any network-related questions or queries, where a number of");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- volunteer staff and helpful users will be happy to assist you.");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :-  ");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- You can meet us at FOSSCON (http://www.fosscon.org) where we get");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- together with like-minded FOSS enthusiasts for talks and");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- real-life collaboration.");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :-  ");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- We would like to thank Private Internet Access");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- (https://www.privateinternetaccess.com/) and the other");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- organisations that help keep freenode and our other projects");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- running for their sustained support.");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :-  ");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- In particular we would like to thank the sponsor");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :- of this server, details of which can be found above.");
		new MessageHandler(":kornbluth.freenode.net 372 matty_r :-  ");
		new MessageHandler(":kornbluth.freenode.net 376 matty_r :End of /MOTD command.");
		new MessageHandler(":matty_r MODE matty_r :+i");
		new MessageHandler(":NickServ!NickServ@services. NOTICE matty_r :This nickname is registered. Please choose a different nickname, or identify via /msg NickServ identify <password>.");
		new MessageHandler(":matty_r!~urChatCli@86.98.4.132 JOIN ##sharepoint");
		new MessageHandler(":kornbluth.freenode.net 332 matty_r ##sharepoint :Sharepoint! That product you love to hate");
		new MessageHandler(":kornbluth.freenode.net 333 matty_r ##sharepoint jrich523!~IceChat9@mail2.apexlearning.com 1391470269");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = ##sharepoint :matty_r pxed jrich523 Ulgar Aoyagi_trashtop ronx Surt_Q HIghoS DarkGhost julieeharshaw tropicana spcore Jedicus Sl0vi mathu baloney");
		new MessageHandler(":kornbluth.freenode.net 366 matty_r ##sharepoint :End of /NAMES list.");
		new MessageHandler(":matty_r!~urChatCli@86.98.4.132 JOIN #gamedev");
		new MessageHandler(":kornbluth.freenode.net 332 matty_r #gamedev :You must be identified to speak. | http://youcanmakevideogames.com/ | http://compohub.net/");
		new MessageHandler(":kornbluth.freenode.net 333 matty_r #gamedev piman!piman@kai.vm.bytemark.co.uk 1393509398");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = #gamedev :matty_r JohnPois_ ankit9 ananasblau regreg Axy Orion] Peppercorn1421 Neomex Sendoushi daedeloth Noldorin phinxy theodon7473 refs pulse GoodOldJacob the8thark rnx Bwild Kasu pestle_ caribou| Jellydog vocodork urraka atomekk BearishMushroom AJGrogers workerbee ShawnWhite tomreyn Adib Alina-malina KillerJim kahrl toxicFork Kalias ikrima poxip Haswell JohnPoison ShadowIce cysm Isolol Xe Vbitz Kaze|reaL knod_away Bigcheese olfox_ jeaye Purebe voldyman");
		new MessageHandler("':kornbluth.freenode.net 353 matty_r = #gamedev :jerkface0 Kitaete RoadKillGrill newguise1234 shingshang Joeh w4ffles ivan\' redpill bhldev faty nx5 sparetire_ JTF195 ninzine Patzy jdowell mat^2 axion lasserix BlackFox maxorator nnesse graphitemaster drostar_ Khelz stef Axord worfox Pookz thesquib Triplefox dhaber Zeioth mr_lou aewffwea DrBenway ivanf stoopdapoop CJKay BoomerBile sebbu2 howitdo grim001 RudyValencia dau zoktar Trieste rizu Madsy aiBob mire FabioGNR- Sammy8806 SupaHam mGuv o0elise0o");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = #gamedev :Neurisko fella6s 7JTABSFX8 Peixinho Shadda dreda z|Andy mrkake flgr hsknz Scient Sail cmbrnt Enthralled fydo Xark jenskarlsen UziMonkey l1ght BlastHardcheese programmerq paracyst dvoid envi_un jacres_ tanjoodo seg King_DuckZ HiggsPossum_ @ChanServ hayer icedp Khruu Rutix panzana` unreal ExpiredPopsicle freanux bkc_ SudsDev_ Brend iamtakingiteasy Guest38445 valevale AcerBoson Zerot burgobianco enleeten dman_ LiamM Karethoth Motig ra4king Spark yomiel");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = #gamedev :Shoozza jerev sam Vladimir1ki pastry Jessicatz_ Aster Sembiance djinni The_Fly");
		new MessageHandler(":kornbluth.freenode.net 366 matty_r #gamedev :End of /NAMES list.");
		new MessageHandler(":matty_r!~urChatCli@86.98.4.132 JOIN #reddit-gamedev");
		new MessageHandler(":kornbluth.freenode.net 332 matty_r #reddit-gamedev :http://reddit.com/r/gamedev | http://reddit.com/r/gamedevscreens | DrAwesomeclaw's irc jam every weekend.  Make a game.");
		new MessageHandler(":kornbluth.freenode.net 333 matty_r #reddit-gamedev jeffz` 1415951575");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = #reddit-gamedev :matty_r suehle Arrkangel_ ankit9 Inside +Sadale ananasblau coldacid Waynes1 SteamPunk_Devil Jacktwo Cervator +LordNed|Hiccup goodenough MageJames enitiz dysoco +zhov samrat Neomex zidsal tehsma napper phinxy Gamecubic rnx RyanPridgeon Raziel +Jonas__ LDAshMP_ b4rbz g9icy tus Khlorghaal Maruinslun FLHerne +caribou| deniska zahlman Jellydog +urraka Sebsebeleb Tesrym Guest8648 +ShawnWhite SequenceK Frankie1111 Adib Alina-malina Nort +toxicFork");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = #reddit-gamedev :lritter__ t7 Kalias +jfoxzzz ikrima Gilgamesh17 ImQ009 K59 x00000000 JustinPierce_ Giik\\Sleep altered kneeko_ dhw cysm socks_ Isolol skarn nepgear Pietdagamer GamedevBot +jpetrie +dnyy sakirious voldyman kexplo Lord_of_Life Smily +terminx__ wizardhat chrismdp eka Eynx Kitaete Airspace SawJong shingshang Arab- +tm512 C4Cypher faty Floens Staretta JTF195 jdowell mat^2 delaney BlackFox kalz iFire adlan_ X-Scale sebeleb_pi hazardous gamingrobot Khelz");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = #reddit-gamedev :Arrkangel _bryan +Axord Auctus +Lemtzas tomshreds Deoxyribonucleic digiwombat GarbageElitist_ UnicornForest DrBenway ivanf RobStorm stoopdapoop Legiion +hackhalo2 Anxi80 beyzend Ispira k-hos Polygonatron grim001 demanrisu katu sofancy Madsy jason-s3studios zapu deadlugosi discopig +chadams +iximeow Peter Zol_ nupogodi Seich Tapeon mGuv mtsr_zzz twincannon rushh1 Dr3amc0d3r|away samsonious mukunda_ SirCmpwn +jorgenpt +sigtau jyxent dreda Acidic");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = #reddit-gamedev :jeffz +Shammah Genesis2002 Whoop Exadyne Entalyan vehementi______ cron +bartwe twodayslate___ edwardly Bluefoxicy Sail cmbrnt neoman carefish acidi Orava_ Enthralled [swift] oldmanmike mattrepl Kitar|st Narris Ad-man Phase l1ght ahungry mrkishi paracyst melody +kiwibonga T19EL Corpus_C natemi AMorpork josefnpa1|pi tanjoodo vsync HiggsPossum_ Retroid rld| +Amadiro icedp psykon1 Khruu MeWulf wallzero Guest243 danlucraft ExpiredPopsicle Dinnerbone");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = #reddit-gamedev :Me4502 Thinksquirrel guybrush Sommer717_ NullSoldier Amaan +flexd mentalist Spleeze halc AcerBoson YukonAppleGeek Simie Zerot Vertan_ swartulv kaiza enleeten FlyOnTheWall Guest32852 zetaPRIME Motig ra4king Shoozza josefnpat sigveseb HansiHE bitslap rlomax hobblygobbly binarybitme djinni");
		new MessageHandler(":kornbluth.freenode.net 366 matty_r #reddit-gamedev :End of /NAMES list.");
		new MessageHandler(":matty_r!~urChatCli@86.98.4.132 JOIN ##javascript");
		new MessageHandler(":kornbluth.freenode.net 333 matty_r ##javascript gkatsev!~gkatsev@oftn/board/gkatsev 1413089816");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = ##javascript :matty_r hiptobecubic slikts killa_kyle jaspertheghost LucaTM Centime rampr Rejected playjax lpommers joev efco Kephael SP-15 Arieh Axy anekos Waynes1 mikerrrrrr swist Jikan zorak8 TripTastic Blizzy b1rkh0ff mjz13 mblagden- sandstorm TheCowboy` bannakaffalatta Menorah armyriad Jackneill luketeaford rpag cr`nge IvailoStoianov jarcosmonaut goodenough vev intothev01d darkbasic_ laurensvanpoucke ejb RusAlex Amaal enitiz giuseppesolinas gtristan Pattt");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = ##javascript :}ls{ AndChat|616761 cuq NickTheGameboy Sendoushi aeoril sphenxes Jayflux tazle arjunmehta ciwolsey ApplesInArrays sinclair rnx sturmination Zapsoda_ domsz seaned Photism_ cads sergey Bwild Ilhami theREALorthoplex ome Kasu MuffinMan` pimlu Twey knightshade decoponio gelignite ToApolytoXaos jrmoretti agentDio Jellydog stefan-_ chameleonrising1 ericawebdev heedypo artisticMink yansanmo Ronyrun jimmyy Hestil netj robbyoconnor msg Sorella alnewkirk");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = ##javascript :Milkweed iAsterisk Suchorski Stalkr_ metr manuelbieh daedalus_^_ Alina-malina bluesm KillerJim chester boredz gde33 malik jds m8 Trekka12 fotoflo pandaant Left_Turn yeticry austincheney ImQ009 Haswell jacuqesdancona arnoud plutoniix SirLagz eNTi_ Maurice` frd_ KD9AUS Coaster insin mtree kakashiAL robdubya ChristianM simion314_ jxie Meeloow Frosh groms thksb doug64k ht__th thevishy Guest71088 meldron rhp caisah loktar marthinal Awoken benjamingr dni");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = ##javascript :wvdhaute vikash-afk psyprus AgentDis oso96_2000 pragma- Woet rylinaux riotz skarn jzl Klumben cjwelborn blackjid_ seismo ConstantineXVI oohnoitz c0b DanFritz agarwalvivek__ crazybotsmoker roqo agarwalvivek_ iH8Pickles codebrainz gig3m chjj artgoeshere Vbitz zpconn__________ jhm elspru relaxer amatecha ggherdov deranged psy_ erquhart_ sgen fekz_ ljharb dnyy niklasdstrom_ Raynos_ evilbug skyjumper Iveson lif hlindset_ mlee jabbslad noisia___ Kesarr__");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = ##javascript :cagedwisdom TweedleDee ulkesh p0wn3d_mhs C5H8NNaO5 ChoiKyuSang OnkelTem SexualRickshaw bhldev skaag TTrinity fooey ctorp PaulCapestany GreenJello renekooi Floens learner Misan Flannel ValicekB asn kukukuku earldouglas iamtakingiteasy zeekay xivix_ falafel mylord k1ng go|dfish aphid mgpl danecando Robdgreat zamba diginet crdpink DawgMcCrockford BSaboia gambakufu lolmaus liuwenhao Elvano CoJaBo HA5h rcombs Primero jnollette vectra kalz malcolmva");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = ##javascript :TrafficMan_ sboudri__ fRAiLtY- maxorator jdeeburke cythrawll clarkk ShinySides Sergonium rikkus Hausas jpwgarrison glitch100 atomi_ determinant dman777_alter lhdc_ graphitemaster Solid1_afk inimino` khmer_ gkatsev_ zomg PaulePanter clamstar Maxdamantus hazardous sirxemic_ jpossi Mr_Sheesh peterhil kotk_ stef mikeones sboudria_ mbenadda ivanoats Solet phpnode Sna4x8 ben_alman trollboy py0 ciqsteve suspicious_eel blobaum pikaren affa09 alienspy coiner");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = ##javascript :Kurvivor s00pcan nuken_ xintox SFeser shoky TinkerTyper ezakimak jasonmit friss umpc thesquib Lindrian Thinkofdeath codelahoma dhaber tomshreds eric Vivekananda C48I52AG mordof_work Kilo`byte xx2 crydust Guest67216 Icedream zhulikas eighty4 badon mak`` cebor hagb4rd Agen_Terminator m0dest Jardayn semigloss Mateon1 joshskidmore xelra Areks ragas bmn Dramors retornam buu hoyong JustAPoring oojacoboo bfoxwell cpc26 BoomerBile yazdmich aulait");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = ##javascript :tirengarfio HewloThere mbeasley JavaChip Gaybuddies shauno Sasha PotPlant_ rawplaye1 srcerer Asandari nemesit|znc colwem Tawre onthesta1rs lrvick StephenS phelix Shayanjm necrogami doc|home fenduru danemacmillan Kartagis dellai Takumo Sgeo teejar MatheusOl katu matp Khisanth Trieste Iota Cheery AzaToth rzec nisstyre duckson SebastianFlyte dw1 kriskropd e2xistz AcTiVaTe LMNOP Floatingman antons simius tkjaergaard hipsterslapfigh- ungage zivc wdgreg");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = ##javascript :richardwhiuk Fusl erry chovy YamakasY ehalas DFlat AKPWD blahsphemer Seich SjB TDJACR Somatt psacrifice Slim EvRide1 verma n1cky fel spossiba tsunamie Wirewraith tauren secrettriangle Artpicre SupaHam KindOne Jahm johnnyfive techwharf drager tobmaster brotatos glebihan_ RichardJ varesa Adeon hendricius hagabaka karstensrage Guest62947 krypten SimonP86 fella6s manimal GnarlyBob cpayan NemesisD SirCmpwn timbabwe rhalff Exagone313 danneu AlecksG joeyDB");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = ##javascript :the-gibson regreddit nezZario felixjet ClockCat slide anglisc njbair Livadi tekzilla derrzzaa Shagane patrykn akkufomk hollusion ppppaul deltab joepie91 joshfinnie runeks_ CPF0000 sud0x3_ BaNzounet PigDude mitwilsch_ spenguin klltkr_ bleepy Danavu Alex` warreng kokel GarethAdams sayakb kraucrow resure xrickx bhughes jonrh_ ludkille- mize telephone Exadyne Taylor felipesabino aewing_ speakingcode Toriad apipkin_ ndeeah foamz mcav jneen|afk");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = ##javascript :patocallaghan DjMadness ceej_irccloud k4r1m antoxyz fumk master5o1 jscinoz plantain timbur o0elise0o marionebl_ Xoro tris twisted` mattattaque norm hooloovoo hipsterslapfight Outsider_ ws2k3 ZucchiniZe kaplejon_ alamar mikaelb phishy casual_ lebster dotty Raccoon Kinny edwardly Tee_Pee __main__ marmalodak Nothing4You ohcibi Moult_ tortal jave voltagex zouave bl4ckdu5t Gege log0ymxm nanashiRei alexherbo2 Chunk2 bcjordan RawBin Gregor sabajo dmamills");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = ##javascript :raj nakano worc roo lance|afk rickibalboa Scient Cheffheid tongcx cjohnson mkitson TheEternalAbyss qpls_ ZoidbergWill_ jiffe ooggllbbee exonumia DarkGhost hja Beautato abrkn\\ shoerain slavik0329 jmoney Enthralled NinjaBanjo Lebbe bentruyman eagles0513875 Viriatus angryzor stevenola soahccc CentaurWarchief hennilu loglaunch hhkb prawnsalad Tycale wodim lykkin kevireilly revolve AlexZ Gilgameshkun ajf- Gronghon nodedfree spline joopmicroop dnordstr1");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = ##javascript :pingo McJesus moijk hfp PrincessAuv jeffw198____ parshap elijah oogaw yetAnotherZero jekt TheCowboy kernal Typo lifenoodles AciD` Brewster hxm jriese t Whiskey wolf_mozart th2389_____ tapout l1ght nerdfyr chuckharmston frozenice_ ahungry sharkhat yhsiang daurnimator borkdude sparr kanzure mrkishi Segaja mu venuatu Niamkik werkschau guzzlefry johannes_ devJunk stylus Ouchy bwright__ cYmen_ jayne mage_ BombStrike Fogest sailerboy variable Platini");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = ##javascript :Seikho Tabmow sazzer jvhester_ Aero mitzip makii-42 oNeDaL getxsick aef_ natemi Guest62785 RichardBronosky Ulrike_Rayne Internet13 dflurker JmZ_ StevenMcD acidjazz cryptoca_ greg5gre_ kba spacepluk interru ripdog tyteen4a03 seg krizoek benaiah nathan7 franckbenoit DrForr cody-- crewshin Meeh xMopxShell comboy kevr jcool _br_ xilyin mist elmcrest belak ahmed_ tomalak RoryHughes vcoinminer Alcubierre Cork gf3 DrLou helgrima jscarmona hfp_work Nach0z");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = ##javascript :masscrx bailon ansu deg Tarential Kaltiz rburgosnavas lijnen_ ldlework leptonix eshlox daredev clode__ johnkpaul deimos__ Obi_obi stemount maZtah dorey patrio bbhoss featheryahn MoDFoX____ doebi gildean Praise silverraindog CreativeWolf michael_mbp caveat- palid walbo FaresKAlaboud debx sukima pll^ EdwardIII Beg av0idz cibs Vorisi Killberry w0ng crxz0193 emma gtc gattuso z16 Sheraf Guest27136 Linkeh_ AphelionZ kirjs______ dziga Kenzi` Cerise bpierre");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = ##javascript :imslavko thedonut stoodder_____ noahsussman hguux_ garndt fhd_______ Amaan catsup m4ggus hacfi introom Davlefou sari1 SirFunk jacobroufa olli- bgy znf Nickwiz Kaimei samiy thamz Dwarf cellybot Depe averell roentgen Pagan zz_night-owl zylum zly ServerSage tacotaco_ bjarneo Spleeze hnsr yorick ozh benjick Eadaro Naeblis preaction ChaozHenchman rosseaux Snakes mpajor marahin merlin83 HuiJun robmozart_ phaer ajpiano arkin ungage` paul_k Bish oddalot");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = ##javascript :MaekSo netman StingRay` alexwhitman JackPH Cydrobolt eagleflo anafrogue meder_ eichiro heyimwill Esya kborchers Ring0` vlad_starkov renetool__ tortib Jonno_FTW M-ou-se Armen b1nd Retrace _genuser_ dig1tal markand cuqa Bleadof ormaaj ManneW Floyd_ cresso ejnahc rjx herme5 ix007 callumacrae _rgn Xgc xer0x DDR Lulzon sentry adam12 accelerate tioxhiz rymate1234 kevinfagan dualbus monokrome snakeater maltris PhonicUK marcel bewl Ownix waspinator_ patson");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = ##javascript :Cathy knix marienz TViernion jerev ericwood mblagden_ Kage reid Happzz TheMoonMaster skinny_much quenoz mkander_ pastry kinesis Teudjy jaredrhine bact baloney insel brenna m0 delta6 jory tomaso enhance nikolah substack benth mbor rzeka bayleaf dbolser luite zonetti fold Johnny- Aster therealklanni ratsupremacy joeytwiddle _ds82 kbrosnan xlii mafi sosby nicelife percival yeltzooo yosafbridge pkiller Naive uzi boltR smathews milkandtang emid_ digiwano");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = ##javascript :tomaw");
		new MessageHandler(":kornbluth.freenode.net 366 matty_r ##javascript :End of /NAMES list.");
		new MessageHandler(":matty_r!~urChatCli@86.98.4.132 JOIN #Powershell");
		new MessageHandler(":kornbluth.freenode.net 332 matty_r #Powershell :Welcome to the Virtual PowerShell User Group. Feel free to ask questions, and remember http://meta.stackexchange.com/help/be-nice | Use http://PoshCode.com for long code examples :)");
		new MessageHandler(":kornbluth.freenode.net 333 matty_r #Powershell Jaykul!~Jaykul@191.236.135.26 1418836178");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = #Powershell :matty_r Dralock RobotsOnDrugs Jeevan pxed ^wald0 Slayman exixt MrAlexanB hugaraxia spuk +jrich523 perilsgate_ Ulgar polack Palmar Clawdroid manuel_ kate-o sithfm SoreGums DeMiNe0 mwjcomputing +PowerSchill Azulez pf_moore Varazir jballard Payhn altdev LarchZERO cliluw Saubatzen et0x_ Mindfart unop Hydr0p0nX silentfury PZt doubledutch Nebulis01_Work nekomech DollarUnderscore Sofapute lubyou Stuwee_AFK cartwright sgstair OneBallWonder Aurock sudormrf");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = #Powershell :quantumfoam Gaurhoth Guest61767 ciborg Vile` ShouldBeAFK angrynerd HIghoS zama baristatam julieeharshaw crow Hodge redyey ccp Adran olspookishmagus ele butyoudonot Nebulis01|Home Whiskey phuzion clajo04 jidar Goofmobber harmj0y Q_Continuum cyborg_ darksim915 alex_650 tvsutton_ sunrunner20 Kittz_ slumos- zbrown mota @ChanServ +Bergle_1 tang^ clutch_ BobFrankly Lubinski sasilik Trbo eirirs paradizelost makson JarianGibson rubenb entteri heinrich5991");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r = #Powershell :smeaaagle Grelot sntxrr davidski zaf romangeeko AlHafoudh ssnova zespri_host1 CaptainPunchFACE MrPockets zalran midacts redyeyw stephenh VSpike chilversc bucketm0use Snet2 xhoy mulvane doxinho joga prooz Zerqent chipolux Cope ekkelett early +sepeck emid_");
		new MessageHandler(":kornbluth.freenode.net 366 matty_r #Powershell :End of /NAMES list.");
		new MessageHandler(":kornbluth.freenode.net 477 matty_r ##java :Cannot join channel (+r) - you need to be identified with services");
		new MessageHandler(":kornbluth.freenode.net 477 matty_r ##java :Cannot join channel (+r) - you need to be identified with services");
		new MessageHandler(":ChanServ!ChanServ@services. NOTICE matty_r :[#gamedev] Game Development Channel || Not affiliated with gamedev.net (#gamedev on irc.afternet.org)");
		new MessageHandler(":services. 328 matty_r #powershell :http://PowerShellGroup.org/virtual");
		new MessageHandler(":NickServ!NickServ@services. NOTICE matty_r :You are now identified for matty_r.");
		new MessageHandler(":matty_r!~urChatCli@86.98.4.132 JOIN ##java");
		new MessageHandler(":kornbluth.freenode.net 332 matty_r ##java :Welcome! || Read http://javachannel.org/ before participating. || Paste limit is two lines; ~pastebin lists options. || No applets, please. || Minecraft, Android, and Javascript all have their own channel.");
		new MessageHandler(":kornbluth.freenode.net 333 matty_r ##java dreamreal 1413801904");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r @ ##java :matty_r AngelKing47 Ants Rashid_f Maldivia theshado_ djhworld joev james41382 Kephael Shiina Hanii momomo sandstorm DVass armyriad fosterbuster Jackneill atomx Atque idletask fatguylaughing rgr MartialLaw lexek_ Fiki Ironlink Neomex sphenxes ApplesInArrays ferret_ deSilva vinleod theREALorthoplex Raziel Unicorn437 Jonas__ grigoriy_spb wd40s gelignite zoraj fstd coolcusty neo1691 llorllale Optic heedypo robbyoconnor Suchorski Alina-malina m8 plitter");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r @ ##java :Clete2 deepy celeritas k5_ kermit elliotd123 anunnaki SynrG GEEGEEGEE linkd deSouza paulweb515 Zorkmid sross07 Artemios zsentinel ingenious Natch maxorator Sergonium Hausas DaGardner Deaygo cythrawll hazardous dmlloyd_ phantomcircuit egp MjrTom Kabal619 patarr mparisi ctrlrsf sjmikem yokel tactile sir_galahad_ad sarkie Gravitron lord4163 kktg gratimax TinkerTyper apetresc Lindrian raztok sonOfRa Vivekananda javabot nallar xx2 bolt cebor Sarodj blSnoopy");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r @ ##java :scav moviuro Rallias fluidr ngc0202 Dramors spjt aboudreault genpaku javier obiwahn prc LewsTherin Lothlann fxkr slidercrank avocado pingveno wawb rayhaan_ DrDuck sebash necrogami howitdo grim001 dmlloyd mrbitsandbytes quelqun_dautre mitch0 Sgeo DeadSix27 Nothing4You_ Candle shmoon amitev Marlinski adimit LittleFool kavent wordtodabird Janhouse deuteros brcolow simius Cyclohexane Bombe uberj Sengoku dan64 ShaRose jalcine Boreeas R0b0t1` vinky Chach_30");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r @ ##java :YaknotiS puff Kage SupaHam KindOne sarbs zalami Nancy2012 phix ktwo r1pp3rj4ck z4tz ron rhonabwy lvh fella6s SniperFodder GnarlyBob PlanckWalk SirCmpwn Exagone313 zml klaas KnightRider00 leslie Bitwise ClockCat indigo Shagane pr3d4t0r ConqueredWarrior udoprog Aenigma evilmidget38 dasilvj theresajayne freem|nd Taylor Stephmw xintron Number2 drdanick JakeWharton zxiiro @cheeser edwardly __main__ [twisti] ohcibi AbleBacon ricky_clarkson ianp JZTech101 raj");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r @ ##java :worc roo teklol @kinabalu ircnode0 lilalinuxHamburg lilalinux obrut Enthralled Rylee NeilHanlon FrederikB aleamb tjsnell melonstorm notore Orphis r3m1 GGMethos weyer PrincessAuv oogaw pakl oddalot jcrites anthony25 lifenoodles aaearon Brewster WizardGed Reventlov groundnuty Sou|cutter Byteflux JavaGeek `Yoda Ulrike_Rayne paracyst matthewt AwesomeDragon Speed` cherwin BombStrike rorx totokaka sazzer mgorbach Makinit Aero extor Irrelium makii-42 CustosLimen");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r @ ##java :camerin Vaxu asm89 tyteen4a03 @ChanServ karstensrage SJr Nahra m1dnight_ Riking marvimias comcor mist belak sgronblo zaltekk Paks ansu asfdd DonRichie troydm Praise zeapo StFS meke kikkerbrood trebuh Jonno_FTW lampe2 ubuntuisloved JyZyXEL misterli djjunde schnippi rs0 dock9 akitada [diecast] Striki Xeru Neptu [cust] xelk Muzer ernimril Sargun surial Chewtoy iamtakingiteasy TheNumb averell zack6849 AlHafoudh btipling JPT Maxel gdrc Spec ChaozHenchman");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r @ ##java :manitu sproingie cobolfoo sess acuzio Disconsented d1b_ insel odinsbane HylianSavior StingRay` Jan1337z disturbedmaggot bendem Eburitus brainproxy dbck pandzilla Ring0` Kester M-ou-se LiamM zh32 Jatoskep nb-ben @dreamreal softmetz ormaaj fr0ggler ix007 Xgc kungp rubas Dessimat0r jink dualbus MindlessDrone deebo langkid1 Stummi rayfinkle AlexejK Ragnor switch firewyre smola zz_adgtl impulse150 rodr1go Sumason diminoten hakvroot ashka xnrand Shapeshifter");
		new MessageHandler(":kornbluth.freenode.net 353 matty_r @ ##java :Deaod elnur hiredman fold joeytwiddle plunkthis yeltzooo ivan` bobey6 tomaw");
		new MessageHandler(":kornbluth.freenode.net 366 matty_r ##java :End of /NAMES list.");
		new MessageHandler(":kornbluth.freenode.net NOTICE ##java :[freenode-info] if you're at a conference and other people are having trouble connecting, please mention it to staff: http://freenode.net/faq.shtml#gettinghelp");
		new MessageHandler(":ChanServ!ChanServ@services. NOTICE matty_r :[##java] Please read the topic for basic channel guidelines. Thanks.");
		new MessageHandler(":jaspertheghost!~jasperthe@c-73-1-208-157.hsd1.fl.comcast.net QUIT :Quit: jaspertheghost");
		new MessageHandler(":matty_r!~urChatCli@86.98.4.132 PART ##sharepoint");
		new MessageHandler(":matty_r!~urChatCli@86.98.4.132 PART #gamedev");
		new MessageHandler(":matty_r!~urChatCli@86.98.4.132 PART #reddit-gamedev");
		new MessageHandler(":matty_r!~urChatCli@86.98.4.132 QUIT :Client Quit");
		new MessageHandler("ERROR :Closing Link: 86.98.4.132 (Client Quit)");
	}
	
	private int posnOfOccurrence(String str, char c, int n) {
	    int pos = 0;
	    int matches = 0;

	    for(char myChar : str.toCharArray()){
	    	if(myChar == c){
	    		matches++;
	    		if(matches == n)
	    			break;
	    	}
	    	pos++;
	    }
	    return pos;
	}
	
	private int countOfOccurrences(String str, char c) {
	    int matches = 0;

	    for(char myChar : str.toCharArray()){
	    	if(myChar == c)
	    		matches++;
	    }
	    return matches;
	}
	
	private Boolean isBetween(String line,char start,String middle,char end) {
		int startIndex = line.indexOf(start);
		int middleIndex = line.indexOf(middle);
		int endIndex = line.substring(startIndex+1).indexOf(end) + startIndex;
		
		if(startIndex >= 0 && middleIndex >= 0 && endIndex >= 0)
			if(middleIndex > startIndex && middleIndex < endIndex)
				return true;

	    return false;
	}

	public static class IDGroup { 
		private final int min, max; 
		private final MessageHandlerBase handlerType;
		private final MessageIdType type = MessageIdType.NUMBER_ID;
		
		public IDGroup(int min, int max,MessageHandlerBase handlerType) { 
			this.min = min; 
			this.max = max; 
			this.handlerType = handlerType;
		}
		
		public boolean inRange(int checkNumber){
			if(checkNumber >= this.min && checkNumber <= this.max)
				return true;
			else return false;
		}
	} 
	
	public static class IDSingle{
		String id;
		int[] idArray;
		MessageHandlerBase handlerType;
		MessageIdType type;
		
		public IDSingle(int id,MessageHandlerBase handlerType) {
			this.idArray = new int[]{id};
			this.handlerType = handlerType;
			type = MessageIdType.NUMBER_ID;
		}
		
		public IDSingle(int[] id,MessageHandlerBase handlerType) {
			this.idArray = id;
			this.handlerType = handlerType;
			type = MessageIdType.NUMBER_ID;
		}
		
		public IDSingle(String id,MessageHandlerBase handlerType) {
			this.id = id;
			this.handlerType = handlerType;
			type = MessageIdType.STRING_ID;
		}
		
		public boolean isEqual(String testId){
			try{
			if(Integer.parseInt(testId) > 0)
				return isEqual(Integer.parseInt(testId));
			} catch(Exception e){
				return id.equals(testId);
			}
			return false;	
		}
		
		public boolean isEqual(int testId){
			for(int x : idArray)
				if(x == testId){
					return true;
				}
			
			return false;
		}
	}
	
	private void addRanges(){
		groupIDs.add(new IDGroup(1,4,new UserRegistrationMessage())); 
		groupIDs.add(new IDGroup(312,322,new CommandResponseMessage()));
		groupIDs.add(new IDGroup(412,415,new BadPrivateMessage()));
		groupIDs.add(new IDGroup(371,376,new GeneralMessage()));
		groupIDs.add(new IDGroup(251,256,new GeneralMessage()));
	}
	
	private void addSingles(){
		singleIDs.add(new IDSingle(5,new NoticeMessage()));
		singleIDs.add(new IDSingle(353,new UsersListMessage()));
		singleIDs.add(new IDSingle(332,new ChannelTopicMessage()));
		singleIDs.add(new IDSingle(477,new JoinFailureMessage()));
		singleIDs.add(new IDSingle((new int[]{366,265,266,250,333,328}),new GeneralMessage()));
		singleIDs.add(new IDSingle("MODE",new ModeMessage()));
		singleIDs.add(new IDSingle("NOTICE",new NoticeMessage()));
		singleIDs.add(new IDSingle("PRIVMSG",new PrivateMessage()));
		singleIDs.add(new IDSingle("PART",new PartMessage()));
		singleIDs.add(new IDSingle("KICK",new KickMessage()));
		singleIDs.add(new IDSingle("JOIN",new JoinMessage()));
		singleIDs.add(new IDSingle(":Closing",new DisconnectMessage()));
		singleIDs.add(new IDSingle("QUIT",new DisconnectMessage()));
	}
	
	public enum MessageIdType{
		NUMBER_ID,STRING_ID
	}
	
	public class Message{
		String prefix;
		String idCommand;
		int idCommandNumber;
		String channel;
		String body;
		MessageIdType type;
		String rawMessage;
		String host;
		String server;
		String nick;
		
		public Message(String fullMessage){
			this.rawMessage = fullMessage;
			setPrefix();
			setChannel();
			setMessageBody();
			setIdCommand();
			setServer();
			setHost();
			setNick();
			
			try{
				this.idCommandNumber = Integer.parseInt(this.idCommand);
				this.type = MessageIdType.NUMBER_ID;
			} catch(Exception e){
				this.type = MessageIdType.STRING_ID;
			}	
			
		}
		
		public String toString(){
			return rawMessage;
		}
		

		private void setPrefix(){
			prefix = rawMessage.split(" ")[0];
		}
		
		private void setNick(){
			if(isBetween(rawMessage,':',"!",'@'))
				this.nick = rawMessage.substring(1, rawMessage.indexOf("!")).trim();
		}
		
		private void setHost(){
			String tempMessage = rawMessage.split(" ")[0];
			if(tempMessage.indexOf('@') > -1)
				this.host = tempMessage.substring(tempMessage.indexOf('@')+1).trim();

		}
		
		private void setServer(){
			String tempMessage = rawMessage.split(" ")[0];
			if(tempMessage.charAt(0) == ':')
				if(countOfOccurrences(tempMessage, '.') == 2)
					this.server = tempMessage.substring(1).trim();
		}
		
		private void setChannel(){
			int messageBegin = posnOfOccurrence(rawMessage, SPACES_AHEAD_DELIMITER, 2);
			
			int channelBegin = rawMessage.indexOf(CHANNEL_DELIMITER);
			if(channelBegin < messageBegin && channelBegin > -1)
				this.channel = rawMessage.substring(channelBegin, messageBegin).split(" ")[0].trim();
		}
		
		private void setMessageBody(){
			try{
			if(countOfOccurrences(rawMessage, ':') > 1)
				this.body = rawMessage.substring(posnOfOccurrence(rawMessage, ':', 2)+1).trim();
			else
				this.body = rawMessage.substring(posnOfOccurrence(rawMessage, ':', 1)+1).trim();
			} catch(IndexOutOfBoundsException e) {
				Constants.LOGGER.log(Level.SEVERE, "Failed to extract a message from received text. " + e.getLocalizedMessage());
			}
		}
		
		private void setIdCommand(){
			idCommand = rawMessage.split(" ")[1];
		}
	}
	
	/**
	 * MessageHandlerBase simply contains two abstract methods to be overridden
	 * @author Matt
	 *
	 */
	public class UserRegistrationMessage implements MessageHandlerBase {
		
		@Override
		public void messageExec(Message myMessage) {
			System.out.println(myMessage.body);
		}

	}
	
	public class CommandResponseMessage implements MessageHandlerBase {
		
		
		@Override
		public void messageExec(Message myMessage){
			System.out.println(myMessage.body);
		}
	}
	
	public class GeneralMessage implements MessageHandlerBase {
		
		
		@Override
		public void messageExec(Message myMessage){
			System.out.println(myMessage.body);
		}
	}
	
	public class JoinMessage implements MessageHandlerBase {
		
		
		@Override
		public void messageExec(Message myMessage){
			System.out.println(myMessage.body);
		}

	}
	
	public class ChannelTopicMessage implements MessageHandlerBase {
		
		
		@Override
		public void messageExec(Message myMessage){
			System.out.println(myMessage.body);
		}
	}
		
	public class UsersListMessage implements MessageHandlerBase {
		
		
		@Override
		public void messageExec(Message myMessage){
			System.out.println(myMessage.body);
		}
	}
	
	public class JoinFailureMessage implements MessageHandlerBase {
		
		
		@Override
		public void messageExec(Message myMessage){
			System.out.println(myMessage.body);
		}

	}
	
	public class ModeMessage implements MessageHandlerBase {
		
		
		@Override
		public void messageExec(Message myMessage){
			System.out.println(myMessage.body);
		}
	
	}
	
	public class ServerChangeMessage implements MessageHandlerBase {
		
		
		@Override
		public void messageExec(Message myMessage){
			
		}

	}
	
	public class BadPrivateMessage implements MessageHandlerBase{

		@Override
		public void messageExec(Message myMessage) {
			// TODO Auto-generated method stub
			
		}

		
	}
	
	public class NoticeMessage implements MessageHandlerBase{

		@Override
		public void messageExec(Message myMessage) {
			System.out.println(myMessage.body);
		}


		
	}
	
	public class PrivateMessage implements MessageHandlerBase{

		@Override
		public void messageExec(Message myMessage) {
			// TODO Auto-generated method stub
			
		}


	}
	
	public class PartMessage implements MessageHandlerBase{

		@Override
		public void messageExec(Message myMessage) {
			// TODO Auto-generated method stub
			
		}

		
	}
	
	public class KickMessage implements MessageHandlerBase{

		@Override
		public void messageExec(Message myMessage) {
			// TODO Auto-generated method stub
			
		}

		
	}
	
	public class DisconnectMessage implements MessageHandlerBase{

		@Override
		public void messageExec(Message myMessage) {
			if(myServer.getNick().equals(myMessage.nick)){
			gui.quitServer(myServer);
			for(Handler tempHandler:Constants.LOGGER.getHandlers())
				tempHandler.close();
			}
		}
	}

	private void handleDefault(String message) {
		System.out.println("Unhandled: "+message);
	}
	
	
}
