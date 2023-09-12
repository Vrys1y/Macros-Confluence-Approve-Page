import com.atlassian.mail.Email
import com.atlassian.mail.server.SMTPMailServer
import com.atlassian.sal.api.component.ComponentLocator
import com.atlassian.confluence.mail.ConfluenceMailServerManager
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response
import groovy.transform.BaseScript
import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import com.atlassian.user.UserManager
import com.atlassian.user.User


@BaseScript CustomEndpointDelegate delegate

sendEmail(httpMethod: "GET", groups: ["confluence-users"]) { MultivaluedMap queryParams ->
    String pageId = queryParams.getFirst("pageId")
    String subject = "Approve notification"
    String body = "Документ ожидает Вашего согласования: " + "https://DUMMY.CONF.URL/pages/viewpage.action?pageId=" + pageId
    String approversList = queryParams.getFirst("approvers")

    for (String userName : approversList.split(",")) {
        funcSendEmail(userName, subject, body)
    }
}

def funcSendEmail(String userName, String subject, String body) {
    def confluenceMailServerManager = ComponentLocator.getComponent(ConfluenceMailServerManager)
    SMTPMailServer mailServer = confluenceMailServerManager.getDefaultSMTPMailServer()

    def userManager = ComponentLocator.getComponent(UserManager)
    def user = userManager.users.find{it.getName() == userName}

    String emailAddr = user.getEmail()

    if (mailServer) {
        Email email = new Email(emailAddr)
        email.setSubject(subject)
        email.setBody(body)
        mailServer.send(email)
    } else {
        throw new RuntimeException("Some error!")
    }
}