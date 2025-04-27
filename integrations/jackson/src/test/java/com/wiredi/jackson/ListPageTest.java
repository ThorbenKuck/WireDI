package com.wiredi.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wiredi.integration.jackson.pagination.PaginationModule;
import com.wiredi.runtime.collections.pages.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ListPageTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .registerModule(new PaginationModule());

    public static List<Arguments> requestArguments() {
        return List.of(
                arguments("""
                        {
                            "pageable": {
                                "paged": false
                            }
                        }
                        """, new Request(Pageable.unpaged())),
                arguments("""
                        {
                            "pageable": {}
                        }
                        """, new Request(Pageable.unpaged())),
                arguments("""
                        {
                            "pageable": {
                                "pageNumber": 0,
                                "pageSize": 10
                            }
                        }
                        """, new Request(new Paged(0, 10, Sort.unsorted()))),
                arguments("""
                        {
                            "pageable": {
                                "pageNumber": 0,
                                "pageSize": 10,
                                "sort": null
                            }
                        }
                        """, new Request(new Paged(0, 10, Sort.unsorted()))),
                arguments("""
                        {
                            "pageable": {
                                "pageNumber": 0,
                                "pageSize": 10,
                                "sort": [
                                    {
                                        "property": "test"
                                    }
                                ]
                            }
                        }
                        """, new Request(new Paged(0, 10, Sort.by("test")))),
                arguments("""
                        {
                            "pageable": {
                                "pageNumber": 0,
                                "pageSize": 10,
                                "sort": {
                                    "property": "test"
                                }
                            }
                        }
                        """, new Request(new Paged(0, 10, Sort.by("test")))),
                arguments("""
                        {
                            "pageable": {
                                "pageNumber": 0,
                                "pageSize": 10,
                                "sort": "test"
                            }
                        }
                        """, new Request(new Paged(0, 10, Sort.by("test")))),
                arguments("""
                        {
                            "pageable": {
                                "pageNumber": 0,
                                "pageSize": 10,
                                "sort": [
                                    {
                                        "property": "test",
                                        "direction": "ASC"
                                    }
                                ]
                            }
                        }
                        """, new Request(new Paged(0, 10, Sort.by(Sort.Direction.ASC, "test")))),
                arguments("""
                        {
                            "pageable": {
                                "pageNumber": 0,
                                "pageSize": 10,
                                "sort": [
                                    {
                                        "property": "test",
                                        "direction": "DESC"
                                    }
                                ]
                            }
                        }
                        """, new Request(new Paged(0, 10, Sort.by(Sort.Direction.DESC, "test")))),
                arguments("""
                        {
                            "pageable": {
                                "pageNumber": 0,
                                "pageSize": 10,
                                "sort": {
                                    "property": "test",
                                        "direction": "ASC"
                                }
                            }
                        }
                        """, new Request(new Paged(0, 10, Sort.by(Sort.Direction.ASC, "test")))),
                arguments("""
                        {
                            "pageable": {
                                "pageNumber": 0,
                                "pageSize": 10,
                                "sort": {
                                    "property": "test",
                                        "direction": "DESC"
                                }
                            }
                        }
                        """, new Request(new Paged(0, 10, Sort.by(Sort.Direction.DESC, "test"))))
        );
    }

    @Test
    public void serializationWorks() throws JsonProcessingException {
        // Arrange
        Pageable pageable = Pageable.ofSize(10);
        ListPage<TestObject> page = new ListPage<>(10, 100, List.of(
                new TestObject(1),
                new TestObject(2),
                new TestObject(3),
                new TestObject(4),
                new TestObject(5),
                new TestObject(6),
                new TestObject(7),
                new TestObject(8),
                new TestObject(9),
                new TestObject(10)
        ), pageable);

        // Act
        String pageString = objectMapper.writeValueAsString(page);

        // Assert
        System.out.println(pageString);
        objectMapper.readValue(pageString, new TypeReference<Page<TestObject>>() {});
    }

    @ParameterizedTest
    @MethodSource("requestArguments")
    public void requestSerializationTest(String raw, Request expected) throws JsonProcessingException {
        Request actual = objectMapper.readValue(raw, Request.class);
        assertEquals(expected, actual);
    }

    public record TestObject(int index) {
    }
}