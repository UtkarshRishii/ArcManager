-- ============================================================
-- ArcManager: 002_rls_policies.sql
-- Row Level Security (RLS) Policies
-- Ensures all user data is strictly scoped to auth.uid()
-- ============================================================

-- Enable RLS on all tables
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.clients ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.projects ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.bank_accounts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.payment_schedules ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.attachments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.audit_logs ENABLE ROW LEVEL SECURITY;

-- 1. PROFILES POLICIES
CREATE POLICY "Users can view own profile"
    ON public.profiles FOR SELECT
    USING (auth.uid() = id);

CREATE POLICY "Users can update own profile"
    ON public.profiles FOR UPDATE
    USING (auth.uid() = id);

CREATE POLICY "Users can insert own profile"
    ON public.profiles FOR INSERT
    WITH CHECK (auth.uid() = id);

-- 2. CLIENTS POLICIES
CREATE POLICY "Users can view own clients"
    ON public.clients FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own clients"
    ON public.clients FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own clients"
    ON public.clients FOR UPDATE
    USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own clients"
    ON public.clients FOR DELETE
    USING (auth.uid() = user_id);

-- 3. PROJECTS POLICIES
CREATE POLICY "Users can view own projects"
    ON public.projects FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own projects"
    ON public.projects FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own projects"
    ON public.projects FOR UPDATE
    USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own projects"
    ON public.projects FOR DELETE
    USING (auth.uid() = user_id);

-- 4. BANK ACCOUNTS POLICIES
CREATE POLICY "Users can view own bank accounts"
    ON public.bank_accounts FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own bank accounts"
    ON public.bank_accounts FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own bank accounts"
    ON public.bank_accounts FOR UPDATE
    USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own bank accounts"
    ON public.bank_accounts FOR DELETE
    USING (auth.uid() = user_id);

-- 5. PAYMENT SCHEDULES POLICIES
CREATE POLICY "Users can view own payment schedules"
    ON public.payment_schedules FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own payment schedules"
    ON public.payment_schedules FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own payment schedules"
    ON public.payment_schedules FOR UPDATE
    USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own payment schedules"
    ON public.payment_schedules FOR DELETE
    USING (auth.uid() = user_id);

-- 6. PAYMENTS POLICIES
CREATE POLICY "Users can view own payments"
    ON public.payments FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own payments"
    ON public.payments FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own payments"
    ON public.payments FOR UPDATE
    USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own payments"
    ON public.payments FOR DELETE
    USING (auth.uid() = user_id);

-- 7. ATTACHMENTS POLICIES
CREATE POLICY "Users can view own attachments"
    ON public.attachments FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own attachments"
    ON public.attachments FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete own attachments"
    ON public.attachments FOR DELETE
    USING (auth.uid() = user_id);

-- 8. NOTIFICATIONS POLICIES
CREATE POLICY "Users can view own notifications"
    ON public.notifications FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can update own notifications"
    ON public.notifications FOR UPDATE
    USING (auth.uid() = user_id);

-- 9. AUDIT LOGS POLICIES
CREATE POLICY "Users can view own audit logs"
    ON public.audit_logs FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own audit logs"
    ON public.audit_logs FOR INSERT
    WITH CHECK (auth.uid() = user_id);
