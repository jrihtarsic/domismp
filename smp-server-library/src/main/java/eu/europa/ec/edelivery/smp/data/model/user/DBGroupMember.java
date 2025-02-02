/*-
 * #START_LICENSE#
 * smp-server-library
 * %%
 * Copyright (C) 2017 - 2024 European Commission | eDelivery | DomiSMP
 * %%
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * [PROJECT_HOME]\license\eupl-1.2\license.txt or https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 * #END_LICENSE#
 */
package eu.europa.ec.edelivery.smp.data.model.user;

import eu.europa.ec.edelivery.smp.data.enums.MembershipRoleType;
import eu.europa.ec.edelivery.smp.data.model.BaseEntity;
import eu.europa.ec.edelivery.smp.data.model.CommonColumnsLengths;
import eu.europa.ec.edelivery.smp.data.model.DBGroup;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;

import javax.persistence.*;

import static eu.europa.ec.edelivery.smp.data.dao.QueryNames.*;

/**
 * Group member authorization  table
 *
 * @author Joze Rihtarsic
 * @since 5.0
 */
@Entity
@Audited
@Table(name = "SMP_GROUP_MEMBER",
        indexes = {@Index(name = "SMP_GRP_MEM_IDX", columnList = "FK_GROUP_ID, FK_USER_ID", unique = true)
        })

@NamedQuery(name = QUERY_GROUP_MEMBER_ALL, query = "SELECT u FROM DBGroupMember u")
@NamedQuery(name = QUERY_GROUP_MEMBER_BY_USER_GROUPS_COUNT, query = "SELECT count(c) FROM DBGroupMember c " +
        " WHERE c.user.id = :user_id AND c.group.id IN (:group_ids)")
@NamedQuery(name = QUERY_GROUP_MEMBER_BY_USER_DOMAIN_GROUPS_COUNT, query = "SELECT count(c) FROM DBGroupMember c JOIN c.group.domain d " +
        " WHERE c.user.id = :user_id AND d.id = :domain_id")
@NamedQuery(name = QUERY_GROUP_MEMBER_BY_USER_GROUPS, query = "SELECT c FROM DBGroupMember c " +
        " WHERE c.user.id = :user_id AND c.group.id IN (:group_ids)")
@NamedQuery(name = QUERY_GROUP_MEMBERS_COUNT, query = "SELECT count(c) FROM DBGroupMember c " +
        " WHERE c.group.id = :group_id")
@NamedQuery(name = QUERY_GROUP_MEMBERS, query = "SELECT c FROM DBGroupMember c " +
        " WHERE c.group.id = :group_id order by c.user.username")
@NamedQuery(name = QUERY_GROUP_MEMBERS_FILTER_COUNT, query = "SELECT count(c) FROM DBGroupMember c " +
        " WHERE c.group.id = :group_id AND (lower(c.user.fullName) like lower(:user_filter) OR lower(c.user.username) like lower(:user_filter))")
@NamedQuery(name = QUERY_GROUP_MEMBERS_FILTER, query = "SELECT c FROM DBGroupMember c " +
        " WHERE c.group.id = :group_id  AND (lower(c.user.fullName) like lower(:user_filter) OR lower(c.user.username) like lower(:user_filter))  order by c.user.username")
@NamedQuery(name = QUERY_GROUP_MEMBER_BY_USER_DOMAIN_GROUPS_ROLE_COUNT, query = "SELECT count(c) FROM DBGroupMember c " +
        " WHERE c.user.id = :user_id AND c.group.domain.id = :domain_id AND c.role= :membership_role ")

public class DBGroupMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "SMP_GROUP_MEMBER_SEQ")
    @GenericGenerator(name = "SMP_GROUP_MEMBER_SEQ", strategy = "native")
    @Column(name = "ID")
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_GROUP_ID")
    private DBGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_USER_ID")
    private DBUser user;

    @Enumerated(EnumType.STRING)
    @Column(name = "MEMBERSHIP_ROLE", length = CommonColumnsLengths.MAX_TEXT_LENGTH_64)
    private MembershipRoleType role = MembershipRoleType.VIEWER;

    public DBGroupMember() {
        //Need this method for hibernate
        // Caused by: java.lang.NoSuchMethodException: eu.europa.ec.edelivery.smp.data.model.DBServiceGroupDomain_$$_jvst7ad_2.<init>()
    }

    public DBGroupMember(DBGroup group, DBUser user) {
        this.user = user;
        this.group = group;
    }


    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DBGroup getGroup() {
        return group;
    }

    public void setGroup(DBGroup group) {
        this.group = group;
    }

    public DBUser getUser() {
        return user;
    }

    public void setUser(DBUser domain) {
        this.user = domain;
    }


    public MembershipRoleType getRole() {
        return role;
    }

    public void setRole(MembershipRoleType role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DBGroupMember that = (DBGroupMember) o;

        return new EqualsBuilder().appendSuper(super.equals(o)).append(id, that.id).append(role, that.role).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(id).append(role).toHashCode();
    }
}
