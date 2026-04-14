#ifndef WINFUSION_LIST_H
#define WINFUSION_LIST_H

#include <stdbool.h>
#include <common/types.h>

struct my_list {
    struct my_list *prev;
    struct my_list *next;
};

void my_list_init(struct my_list *list);

void my_list_insert(struct my_list *list, struct my_list *elm);

void my_list_remove(struct my_list *list);

int my_list_length(const struct my_list *list);

bool my_list_empty(const struct my_list *list);

#define my_list_for_each(pos, head, member)                     \
    for (pos = my_container_of((head)->next, pos, member);      \
    &pos->member != (head);                                     \
    pos = my_container_of(pos->member.next, pos, member))

#define my_list_for_each_safe(pos, tmp, head, member)               \
    for (pos = my_container_of((head)->next, pos, member),          \
        tmp = my_container_of((pos)->member.next, tmp, member);     \
        &pos->member != (head);                                     \
        pos = tmp,                                                  \
        tmp = my_container_of(pos->member.next, tmp, member))

#endif
