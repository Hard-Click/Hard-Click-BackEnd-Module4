package com.wanted.backend.domain.admin_dashboard.application.port;

import com.wanted.backend.domain.admin_dashboard.application.dto.AdminDashboardResult;

public interface AdminDashboardQueryPort {

    AdminDashboardResult findDashboard();

}
