package domiSMPTests.ui;

import ddsl.DomiSMPPage;
import ddsl.enums.Pages;
import ddsl.enums.ResourceTypes;
import domiSMPTests.SeleniumTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.LoginPage;
import pages.administration.editGroupsPage.EditGroupsPage;
import rest.models.DomainModel;
import rest.models.GroupModel;
import rest.models.MemberModel;
import rest.models.UserModel;

import java.util.Arrays;
import java.util.List;

public class EditGroupsPgTests extends SeleniumTest {
    DomiSMPPage homePage;
    LoginPage loginPage;
    EditGroupsPage editGroupPage;
    String domainId;
    UserModel adminUser;
    DomainModel domainModel;
    GroupModel groupModel;
    SoftAssert soft;

    @BeforeMethod(alwaysRun = true)
    public void beforeTest() throws Exception {
        soft = new SoftAssert();
        domainModel = DomainModel.generatePublicDomainModelWithSML();
        adminUser = UserModel.generateUserWithADMINrole();
        groupModel = GroupModel.generatePublicGroup();
        MemberModel adminMember = new MemberModel() {
        };
        adminMember.setUsername(adminUser.getUsername());
        adminMember.setRoleType("ADMIN");

        MemberModel superMember = new MemberModel();
        superMember.setUsername(data.getAdminUsername());
        superMember.setRoleType("ADMIN");

        //create user
        String adminUserId = rest.users().createUser(adminUser).getString("userId");

        //create domain
        domainModel = rest.domains().createDomain(domainModel);

        //add users to domain
        rest.domains().addMembersToDomain(domainModel, adminMember);
        rest.domains().addMembersToDomain(domainModel, superMember);

        //add resources to domain
        List<ResourceTypes> resourcesToBeAdded = Arrays.asList(ResourceTypes.OASIS1, ResourceTypes.OASIS3, ResourceTypes.OASIS2);
        domainModel = rest.domains().addResourcesToDomain(domainModel, resourcesToBeAdded);

        //create group for domain
        groupModel = rest.domains().createGroupForDomain(domainModel, groupModel);

        //add users to groups
        rest.groups().addMembersToGroup(domainModel, groupModel, adminMember);


        homePage = new DomiSMPPage(driver);
        loginPage = homePage.goToLoginPage();
        loginPage.login(adminUser.getUsername(), data.getNewPassword());
        editGroupPage = homePage.getSidebar().navigateTo(Pages.ADMINISTRATION_EDIT_GROUPS);
    }

    @Test(description = "EDTGRP-01 Group admins are able to invite/edit/remove group members", priority = 2)
    public void domainAdminsAreAbleToInviteEditRemoveMembers(){

        UserModel domainMember = UserModel.generateUserWithUSERrole();
        rest.users().createUser(domainMember);

        editGroupPage.selectDomain(domainModel, groupModel);
        //Add user
        editGroupPage.getGroupMembersTab().getInviteMemberBtn().click();
        editGroupPage.getGroupMembersTab().getInviteMembersPopup().selectMember(domainMember.getUsername(), "VIEWER");
        soft.assertTrue(editGroupPage.getGroupMembersTab().getMembersGrid().isValuePresentInColumn("Username", domainMember.getUsername()));

        //Change role of user
        editGroupPage.getGroupMembersTab().changeRoleOfUser(domainMember.getUsername(), "ADMIN");
        String currentRoleTypeOfuser = editGroupPage.getGroupMembersTab().getMembersGrid().getColumnValueForSpecificRow("Username", domainMember.getUsername(), "Role type");
        soft.assertEquals(currentRoleTypeOfuser, "ADMIN");

        //Remove user
        editGroupPage.getGroupMembersTab().removeUser(domainMember.getUsername());
        soft.assertFalse(editGroupPage.getGroupMembersTab().getMembersGrid().isValuePresentInColumn("Username", domainMember.getUsername()));

        soft.assertAll();
    }
}
