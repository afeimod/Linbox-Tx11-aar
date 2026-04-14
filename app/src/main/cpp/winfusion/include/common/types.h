#ifndef WINFUSION_TYPES_H
#define WINFUSION_TYPES_H

#if __STDC_VERSION__ >= 202311L
#define MY_TYPEOF(expr) typeof(expr)
#else
#define MY_TYPEOF(expr) __typeof__(expr)
#endif

#define my_container_of(ptr, sample, member)            \
	(MY_TYPEOF(sample))((char *)(ptr) -                 \
			     offsetof(MY_TYPEOF(*sample), member))

#endif
