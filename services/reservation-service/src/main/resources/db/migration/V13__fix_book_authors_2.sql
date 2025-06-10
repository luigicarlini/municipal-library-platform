/* ---------------------------------------------------------------------------
 * V12 – Fix book authors (2ª tranche)
 * Corregge gli ultimi abbinamenti errati rimasti dopo V11.
 * ------------------------------------------------------------------------- */

-------------------------------------------------------------------------------
-- Dracula → Bram Stoker  (esemplari rimasti)
-------------------------------------------------------------------------------
UPDATE books SET author = 'Bram Stoker'
WHERE id IN (
  '14c16130-6a60-404a-acf8-94608bcae41c', -- Dracula 5
  '0e7414f1-44e0-44b9-ab2b-457d6caa0a29'  -- Dracula 38
);

-------------------------------------------------------------------------------
-- Fahrenheit 451 → Ray Bradbury
-------------------------------------------------------------------------------
UPDATE books SET author = 'Ray Bradbury'
WHERE id IN (
  'b3daed28-db9f-4e27-abdf-c73d7fc75ec8', -- Fahrenheit 451 27
  '6527223f-6059-46a9-9a4c-96b4a63b0fea'  -- Fahrenheit 451 28
);

-------------------------------------------------------------------------------
-- L’isola del tesoro → Robert L. Stevenson
-------------------------------------------------------------------------------
UPDATE books SET author = 'Robert L. Stevenson'
WHERE id = 'aae66fdd-23e7-4a8d-a457-9b2005c5f2b1';

-------------------------------------------------------------------------------
-- Viaggio al centro della Terra 15  → Jules Verne
-------------------------------------------------------------------------------
UPDATE books SET author = 'Jules Verne'
WHERE id = 'd6f5a4b5-9c90-47f2-a85f-8c179e8d42ef';

-------------------------------------------------------------------------------
-- Verifica rapida (facoltativa)
-- SELECT author, COUNT(*) FROM books WHERE title ILIKE 'Dracula%' GROUP BY author;
-- SELECT author, COUNT(*) FROM books WHERE title ILIKE 'Fahrenheit 451%' GROUP BY author;
-------------------------------------------------------------------------------
