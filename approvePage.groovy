import com.atlassian.confluence.pages.PageManager
import com.atlassian.sal.api.ApplicationProperties
import com.atlassian.sal.api.component.ComponentLocator
import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import groovy.transform.BaseScript
import com.atlassian.confluence.core.ContentEntityManager
import com.atlassian.confluence.core.DefaultSaveContext
import com.atlassian.confluence.core.ContentEntityObject
import com.atlassian.confluence.user.ConfluenceUser
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response

@BaseScript CustomEndpointDelegate delegate

def pageManager = ComponentLocator.getComponent(PageManager)
def applicationProperties = ComponentLocator.getComponent(ApplicationProperties)

approvePage(httpMethod: "GET", groups: ["confluence-users"]) { MultivaluedMap queryParams ->

    ConfluenceUser currentUser = AuthenticatedUserThreadLocal.get()

    def pageId = queryParams.getFirst("pageId") as Long
    ContentEntityObject page = pageManager.getPage(pageId)
    def approverKey = "pageApprove" + "-" + pageId + "-" + currentUser.name
    def pageApproveDateKey = "pageApproveDate" + "-" + pageId + "-" + currentUser.name
	def version = page.getVersion() as String
    def currentDate = new Date().format('dd-MM-yyyy') as String

    page.getProperties().setStringProperty(approverKey, "v" + version)
    page.getProperties().setStringProperty(pageApproveDateKey,currentDate)
    
    DefaultSaveContext saveContext = new DefaultSaveContext(true, false, true)

    def currentComment = page.getVersionComment()

    if (currentComment == '') {
        page.setVersionComment("Approved by ${currentUser.name} on ${currentDate}")
    } else {
        page.setVersionComment(currentComment + '\n' + "Approved by ${currentUser.name} on ${currentDate}")
    }

    pageManager.saveContentEntity(page, saveContext)

    Response.temporaryRedirect(URI.create("${applicationProperties.baseUrl}/pages/viewpage.action?pageId=${pageId}")).build()
}