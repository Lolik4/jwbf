/*
 * Copyright 2007 Thomas Stock.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Contributors:
 *
 */
package net.sourceforge.jwbf.mediawiki.actions.queries;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
import net.sourceforge.jwbf.core.actions.Get;
import net.sourceforge.jwbf.core.actions.RequestBuilder;
import net.sourceforge.jwbf.core.actions.util.HttpAction;
import net.sourceforge.jwbf.mapper.XmlElement;
import net.sourceforge.jwbf.mediawiki.ApiRequestBuilder;
import net.sourceforge.jwbf.mediawiki.MediaWiki;
import net.sourceforge.jwbf.mediawiki.actions.util.MWAction;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A abstract action class using the MediaWiki-api's "list=categorymembers ".
 *
 * @author Thomas Stock
 * @see <a href= "http://www.mediawiki.org/wiki/API:Query_-_Lists#categorymembers_.2F_cm">API
 * documentation</a>
 */
abstract class CategoryMembers extends MWAction {

  private static final Logger log = LoggerFactory.getLogger(CategoryMembers.class);

  // TODO do not work with patterns
  private static final Pattern CATEGORY_PATTERN =
      Pattern.compile("<cm pageid=\"(.*?)\" ns=\"(.*?)\" title=\"(.*?)\" />");

  private static final Pattern CONTINUE_PATTERN = Pattern.compile("<query-continue>.*?" //
      + "<categorymembers *cmcontinue=\"([^\"]*)\" */>" //
      + ".*?</query-continue>", Pattern.DOTALL | Pattern.MULTILINE);

  /**
   * constant value for the bllimit-parameter. *
   */
  protected static final int LIMIT = 50;

  protected final MediaWikiBot bot;
  /**
   * information necessary to get the next api page.
   */
  protected String nextPageInfo = null;
  protected boolean hasMoreResults = false;

  protected boolean init = true;
  /**
   * Name of the category.
   */
  protected final String categoryName;

  protected final RequestGenerator requestBuilder;

  protected final ImmutableList<Integer> namespace;
  private final String namespaceStr;

  protected CategoryMembers(MediaWikiBot bot, String categoryName, int[] namespaces) {
    this(bot, categoryName, ImmutableList.copyOf(Ints.asList(namespaces)));
  }

  protected CategoryMembers(MediaWikiBot bot, String categoryName,
      ImmutableList<Integer> namespaces) {
    this.bot = bot;
    this.namespace = namespaces;
    namespaceStr = createNsString(namespaces);
    this.categoryName = categoryName.replace(" ", "_");
    requestBuilder = new RequestGenerator();

  }

  /**
   * generates the next MediaWiki-request (GetMethod) and adds it to msgs.
   *
   * @return a
   */
  protected final Get generateFirstRequest() {
    return new Get(requestBuilder.first(categoryName));
  }

  /**
   * generates the next MediaWiki-request (GetMethod) and adds it to msgs.
   *
   * @param cmcontinue the value for the blcontinue parameter, null for the generation of the
   *                   initial request
   * @return a
   */
  protected Get generateContinueRequest(String cmcontinue) {
    return new Get(requestBuilder.continiue(cmcontinue));
  }

  /**
   * deals with the MediaWiki api's response by parsing the provided text.
   *
   * @param s the answer to the most recently generated MediaWiki-request
   * @return empty string
   */
  @Override
  public String processReturningText(String s, HttpAction action) {
    parseArticleTitles(s);
    parseHasMore(s);
    return "";
  }

  /**
   * gets the information about a follow-up page from a provided api response. If there is one, a
   * new request is added to msgs by calling generateRequest.
   *
   * @param s text for parsing
   */
  private void parseHasMore(final String s) {

    Matcher m = CONTINUE_PATTERN.matcher(s);

    if (m.find()) {
      nextPageInfo = m.group(1);
      hasMoreResults = true;
    } else {
      hasMoreResults = false;
    }

  }

  /**
   * picks the article name from a MediaWiki api response.
   *
   * @param s text for parsing
   */
  private void parseArticleTitles(String s) {

    Optional<XmlElement> errorElement = getRootElementWithError(s).getErrorElement();
    if (errorElement.isPresent()) {
      throw new IllegalStateException(errorElement.get().getAttributeValue("info"));
    }
    Matcher m = CATEGORY_PATTERN.matcher(s);

    while (m.find()) {
      addCatItem(m.group(3), Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
    }
    finalizeParse();
  }

  protected abstract void finalizeParse();

  protected abstract void addCatItem(String title, int pageid, int ns);

  protected class RequestGenerator {

    private static final String CMTITLE = "cmtitle";

    RequestGenerator() {

    }

    String continiue(String cmcontinue) {
      return newRequestBuilder() //
          .param("cmcontinue", MediaWiki.urlEncode(cmcontinue)) //
          .param(CMTITLE, "Category:" + MediaWiki.urlEncode(categoryName)) //
              // TODO: do not add Category: - instead, change other methods' descs (e.g.
              // in MediaWikiBot)
          .build();
    }

    private RequestBuilder newRequestBuilder() {
      ApiRequestBuilder requestBuilder = new ApiRequestBuilder();
      if (namespaceStr.length() > 0) {
        requestBuilder.param("cmnamespace", MediaWiki.urlEncode(namespaceStr));
      }

      return requestBuilder //
          .action("query") //
          .formatXml() //
          .param("list", "categorymembers") //
          .param("cmlimit", LIMIT) //
          ;
    }

    String first(String categoryName) {
      return newRequestBuilder() //
          .param(CMTITLE, "Category:" + MediaWiki.urlEncode(categoryName)) //
          .build();
    }

  }

}
