package com.ericgonzalesevans.tomcatvulnerabilities

import com.amazon.speech.speechlet.IntentRequest
import com.amazon.speech.speechlet.LaunchRequest
import com.amazon.speech.speechlet.Session
import com.amazon.speech.speechlet.SessionEndedRequest
import com.amazon.speech.speechlet.SessionStartedRequest
import com.amazon.speech.speechlet.Speechlet
import com.amazon.speech.speechlet.SpeechletException
import com.amazon.speech.speechlet.SpeechletResponse
import com.amazon.speech.ui.PlainTextOutputSpeech
import com.amazon.speech.ui.Reprompt
import com.amazon.speech.ui.SimpleCard

class TomcatVulnerabilitiesSpeechlet implements Speechlet {
    final static BASE_URL = 'http://www.cvedetails.com/vulnerability-feed.php?vendor_id=45&product_id=887&version_id=0&orderby=1&cvssscoremin=0'

    void onSessionStarted(SessionStartedRequest request, Session session) throws SpeechletException {
        // any initialization logic goes here
    }

    SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
        return getLatestCVEIntent() //gives a greeting as soon as the app is launched
    }

    SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {
        def intent = request.getIntent()
        def intentName = (intent != null) ? intent.getName() : null

        switch (intentName) {
            case "GetLatestCVEIntent":
                return getLatestCVEIntent()
                break
            case "AMAZON.HelpIntent":
                return getNewHelpResponse()
                break
            case "AMAZON.StopIntent":
                PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech()
                outputSpeech.setText("Goodbye")
                return SpeechletResponse.newTellResponse(outputSpeech)
                break
            case "AMAZON.CancelIntent":
                PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech()
                outputSpeech.setText("Goodbye")
                return SpeechletResponse.newTellResponse(outputSpeech)
                break
            default:
                throw new SpeechletException("Invalid Intent")
                break
        }
    }

    void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {
        // any cleanup logic goes here
    }

    private static getLatestCVEIntent() {
        System.setProperty("http.agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

        def cveTitle = ""
        def cveDescription = ""
        def cveDate = ""
        BASE_URL.toURL().withReader { r ->
            def feed = new XmlSlurper(false, false).parse(r).channel.item.first()
            def title = feed.title.toString()
            def description = feed.description.toString()
            def pubDate = feed.pubDate.toString()
            def date = Date.parse("yyyy-MM-dd", pubDate).format("MMMMM d, yyyy")

            cveTitle = title
            cveDescription = description
            cveDate = date
        }

        // Create the Simple card content.
        def card = new SimpleCard()
        card.setTitle(cveTitle)
        card.setContent(cveDescription)

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech()
        speech.setText("The latest vulnerability was posted on ${cveDate}. I have put the details on your Alexa app.")

        return SpeechletResponse.newTellResponse(speech, card)
    }

    private static getNewHelpResponse() {
        def helpText = "You can ask Tomcat Vulnerabilities to get the latest vulnerability, or, you can say exit. What can I do for you?"
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech()
        speech.setText(helpText)

        // Create reprompt.
        Reprompt reprompt = new Reprompt()
        reprompt.setOutputSpeech(speech)

        return SpeechletResponse.newAskResponse(speech, reprompt)
    }
}
