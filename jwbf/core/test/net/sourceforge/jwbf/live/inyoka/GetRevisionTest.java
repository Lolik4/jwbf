//package net.sourceforge.jwbf.live.inyoka;
//
//
//	import static org.junit.Assert.assertTrue;
//import net.sourceforge.jwbf.LiveTestFather;
//import net.sourceforge.jwbf.bots.InyokaWikiBot;
//import net.sourceforge.jwbf.contentRep.SimpleArticle;
//
//import org.apache.log4j.Logger;
//import org.apache.log4j.PropertyConfigurator;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//	public class GetRevisionTest extends LiveTestFather {
//		private Logger log = Logger.getLogger(getClass());
//		private InyokaWikiBot bot;
//		
//		/**
//		 * Setup log4j.
//		 * @throws Exception a
//		 */
//		@BeforeClass
//		public static void setUpBeforeClass() throws Exception {
//			PropertyConfigurator.configureAndWatch("test4log4j.properties",
//					60 * 1000);
//		}
//
//		/**
//		 * Test write and read 
//		 * @throws Exception a
//		 */
//		@Test
//		public final void getRevisionInyoka() throws Exception {
//			bot = new InyokaWikiBot("http://wiki.ubuntuusers.de/");
//			doTest(bot);	
//		}
//		
//		
//		
//		
//		private final void doTest(InyokaWikiBot bot) throws Exception {
//			// TODO not a really good test
//			String label;
//			label = "Startseite";
//			SimpleArticle sa =  bot.readContent(label);
//			
//			assertTrue(sa.getText().length() > 10);	
//			log.info("text: " + sa.getText().substring(0, 10) + "...");
////			assertTrue("editor maybe not okay: " + sa.getEditor(), sa.getEditor().length() > 4);		
//			log.info("author: " + sa.getEditor());
//			log.info("edittime: " + sa.getEditTimestamp());
////			assertTrue("editsumm maybe not okay: " + sa.getEditSummary(), sa.getEditSummary().length() > 4);
//			log.info("editsumm: " + sa.getEditSummary());
//		}
//		
//	}
//
//