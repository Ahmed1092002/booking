package com.example.booking.admin;

import com.example.booking.security.CurrentUserService;
import com.example.booking.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Administrative endpoints for system management")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;
    private final CurrentUserService currentUserService;

    public AdminController(AdminService adminService, CurrentUserService currentUserService) {
        this.adminService = adminService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get system statistics", description = "Retrieve overall system statistics including user counts, booking counts, and revenue metrics")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    public ResponseEntity<Map<String, Object>> getSystemStatistics() {
        Map<String, Object> stats = adminService.getSystemStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Retrieve list of all registered users")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Get audit logs", description = "Retrieve paginated audit logs of system actions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved", content = @Content(schema = @Schema(implementation = AuditLog.class)))
    })
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "50") int size) {
        Page<AuditLog> logs = adminService.getAuditLogs(page, size);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/audit-logs/user/{userId}")
    @Operation(summary = "Get user audit logs", description = "Retrieve audit logs for a specific user")
    public ResponseEntity<List<AuditLog>> getUserAuditLogs(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        List<AuditLog> logs = adminService.getUserAuditLogs(userId);
        return ResponseEntity.ok(logs);
    }

    @PostMapping("/audit-logs")
    @Operation(summary = "Create audit log", description = "Manually create an audit log entry")
    public ResponseEntity<Void> createAuditLog(
            @Parameter(description = "Action description") @RequestParam String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) String details,
            @RequestParam(required = false) String ipAddress) {
        User admin = currentUserService.getCurrentUser();
        adminService.logAction(admin, action, entityType, entityId, details, ipAddress);
        return ResponseEntity.ok().build();
    }
}
