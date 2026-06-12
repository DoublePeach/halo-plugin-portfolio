<template>
  <header class="relative overflow-hidden pt-32 pb-24 md:pt-48 md:pb-32">
    <!-- Decorative background elements -->
    <div class="absolute inset-0 -z-10 bg-pf-bg">
      <div class="absolute top-0 right-0 -translate-y-12 translate-x-1/3 w-[800px] h-[800px] rounded-full bg-pf-primary/5 blur-3xl"></div>
      <div class="absolute bottom-0 left-0 translate-y-1/3 -translate-x-1/3 w-[600px] h-[600px] rounded-full bg-pf-accent/5 blur-3xl"></div>
    </div>

    <div class="pf-container relative z-10">
      <div class="max-w-4xl">
        <div class="inline-flex items-center gap-2 rounded-full bg-pf-surface px-4 py-2 shadow-sm border border-pf-border mb-8">
          <span class="relative flex h-2 w-2">
            <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-pf-primary opacity-75"></span>
            <span class="relative inline-flex rounded-full h-2 w-2 bg-pf-primary"></span>
          </span>
          <span class="text-sm font-medium text-pf-text-muted">Selected Works</span>
        </div>
        
        <h1 class="pf-heading-display text-pf-text">
          Crafting digital experiences<br />
          <span class="text-pf-primary italic font-serif">with purpose.</span>
        </h1>
        
        <p class="mt-8 max-w-2xl text-lg md:text-xl font-normal leading-relaxed text-pf-text-muted">
          A collection of projects spanning microservices architecture, AI applications, and enterprise digital transformation.
        </p>
      </div>
    </div>
  </header>
</template>
