

UPDATE partners SET site_inspection_score = 88.50 WHERE code = 'CAT-101';

UPDATE code_sequences SET next_value = GREATEST(next_value, 200) WHERE scope = 'partners';
UPDATE code_sequences SET next_value = GREATEST(next_value, 200) WHERE scope = 'partner_documents';
UPDATE code_sequences SET next_value = GREATEST(next_value, 200) WHERE scope = 'delivery_persons';
UPDATE code_sequences SET next_value = GREATEST(next_value, 200) WHERE scope = 'partnership_applications';
UPDATE code_sequences SET next_value = GREATEST(next_value, 200) WHERE scope = 'supplier_partnerships';
