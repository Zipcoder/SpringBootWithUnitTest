package com.example.demo;

import com.example.demo.UserController.UserController;
import com.example.demo.config.WebConfig;
import com.example.demo.filter.CORSFilter;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WebConfig.class})
public class UserControllerTest {
    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Before
    public void init(){
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .addFilters(new CORSFilter())
                .build();
    }

    // =========================================== Get All Users ==========================================

    @Test
    public void test_get_all_success() throws Exception {
        List<User> users = Arrays.asList(
                new User(1, "Daenerys Targaryen"),
                new User(2, "John Snow"));

        when(userService.getAll()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].username", is("Daenerys Targaryen")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].username", is("John Snow")));

        verify(userService, times(1)).getAll();
        verifyNoMoreInteractions(userService);
    }

    // =========================================== Get User By ID =========================================

    @Test
    public void test_get_by_id_success() throws Exception {
        User user = new User(1, "Daenerys Targaryen");

        when(userService.findById(1)).thenReturn(user);

        mockMvc.perform(get("/users/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("Daenerys Targaryen")));

        verify(userService, times(1)).findById(1);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void test_get_by_id_fail_404_not_found() throws Exception {
        when(userService.findById(1)).thenReturn(null);

        mockMvc.perform(get("/users/{id}", 1))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findById(1);
        verifyNoMoreInteractions(userService);
    }

    // =========================================== Create New User ========================================

    @Test
    public void test_create_user_success() throws Exception {
        User user = new User("Arya Stark");

        when(userService.exists(user)).thenReturn(false);
        doNothing().when(userService).create(user);

        mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(user)))
                .andExpect(status().isCreated())
                .andExpect(header().string("location", containsString("/users/0")));

        verify(userService, times(1)).exists(user);
        verify(userService, times(1)).create(user);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void test_create_user_fail_404_not_found() throws Exception {
        User user = new User("username exists");

        when(userService.exists(user)).thenReturn(true);

        mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(user)))
                .andExpect(status().isConflict());

        verify(userService, times(1)).exists(user);
        verifyNoMoreInteractions(userService);
    }

    // =========================================== Update Existing User ===================================

    @Test
    public void test_update_user_success() throws Exception {
        User user = new User(1, "Arya Stark");

        when(userService.findById(user.getId())).thenReturn(user);
        doNothing().when(userService).update(user);

        mockMvc.perform(
                put("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(user)))
                .andExpect(status().isOk());

        verify(userService, times(1)).findById(user.getId());
        verify(userService, times(1)).update(user);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void test_update_user_fail_404_not_found() throws Exception {
        User user = new User(999, "user not found");

        when(userService.findById(user.getId())).thenReturn(null);

        mockMvc.perform(
                put("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(user)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findById(user.getId());
        verifyNoMoreInteractions(userService);
    }

    // =========================================== Delete User ============================================

    @Test
    public void test_delete_user_success() throws Exception {
        User user = new User(1, "Arya Stark");

        when(userService.findById(user.getId())).thenReturn(user);
        doNothing().when(userService).delete(user.getId());

        mockMvc.perform(
                delete("/users/{id}", user.getId()))
                .andExpect(status().isOk());

        verify(userService, times(1)).findById(user.getId());
        verify(userService, times(1)).delete(user.getId());
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void test_delete_user_fail_404_not_found() throws Exception {
        User user = new User(999, "user not found");

        when(userService.findById(user.getId())).thenReturn(null);

        mockMvc.perform(
                delete("/users/{id}", user.getId()))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findById(user.getId());
        verifyNoMoreInteractions(userService);
    }

    // =========================================== CORS Headers ===========================================

    @Test
    public void test_cors_headers() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(header().string("Access-Control-Allow-Origin", "*"))
                .andExpect(header().string("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE"))
                .andExpect(header().string("Access-Control-Allow-Headers", "*"))
                .andExpect(header().string("Access-Control-Max-Age", "3600"));
    }

    public static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
