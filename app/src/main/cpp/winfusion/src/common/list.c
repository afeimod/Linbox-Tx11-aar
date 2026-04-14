#include <common/list.h>
#include <stddef.h>

void my_list_init(struct my_list *list)
{
    list->prev = list;
    list->next = list;
}

void my_list_insert(struct my_list *list, struct my_list *elm) {
    elm->prev = list;
    elm->next = list->next;
    list->next = elm;
    elm->next->prev = elm;
}

void wl_list_remove(struct my_list *elm) {
    elm->prev->next = elm->next;
    elm->next->prev = elm->prev;
    elm->next = NULL;
    elm->prev = NULL;
}

int my_list_length(const struct my_list *list) {
    struct my_list *e;
    int count;

    count = 0;
    e = list->next;
    while (e != list) {
        e = e->next;
        count++;
    }

    return count;
}

bool my_list_empty(const struct my_list *list) {
    return list->next == list;
}

void my_list_insert_list(struct my_list *list, struct my_list *other)
{
    if (my_list_empty(other))
        return;

    other->next->prev = list;
    other->prev->next = list->next;
    list->next->prev = other->prev;
    list->next = other->next;
}
